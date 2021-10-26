package ca.qc.bdeb.c5gm.stageplanif;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private Stockage dbHelper;
    private ArrayList<Stage> listeStages = new ArrayList<>();
    private final ArrayList<Stage> listeStagesMasques = new ArrayList<>();
    private RecyclerView recyclerView;
    private ListeStageAdapter StageAdapter;
    private Toolbar toolbar;
    private ItemViewModel viewModel;
    private ArrayList<Integer> selectionPriorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById((R.id.toolbar));
        setSupportActionBar(toolbar);
        dbHelper = Stockage.getInstance(getApplicationContext());
        listeStages = dbHelper.getStages();
        viewModel = new ViewModelProvider(this).get(ItemViewModel.class);
        viewModel.getSelectedItem().observe(this, selection -> {
            listeStages.addAll(listeStagesMasques);
            listeStagesMasques.clear();
            trierListeStage(listeStages);
            selectionPriorites = calculerPrioritesSelectionnees(selection);
            filtrerListeStage(selectionPriorites);
        });

        creationRecyclerView();
    }

    private ArrayList<Integer> calculerPrioritesSelectionnees(int selection) {
        ArrayList<Integer> ListePrioritesSelectionnees = new ArrayList<Integer>();

        for (Priorite p : Priorite.values()) {
            if ((selection & p.getValeur()) > 0) {
                ListePrioritesSelectionnees.add(p.getValeur());
            }
        }
        return ListePrioritesSelectionnees;
    }

    private void filtrerListeStage(ArrayList<Integer> ListePrioritesSelectionnees) {
        for (Stage s : listeStages) {
            if (!ListePrioritesSelectionnees.contains(s.getPriorite().getValeur())) {
                listeStagesMasques.add(s);
            }
        }
        listeStages.removeAll(listeStagesMasques);
        StageAdapter.notifyDataSetChanged();
    }

    private void trierListeStage(ArrayList<Stage> liste) {
        Collections.sort(liste, new StageChainedComparateur(
                new StagePrioriteComparateur(),
                new StageNomComparateur(),
                new StagePrenomComparateur()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.passer_sur_carte) {
            Intent intent = new Intent(this, GoogleMaps.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    /**
     * Cree le recycler view dans l'activitee
     */
    private void creationRecyclerView() {
        recyclerView = findViewById(R.id.rv_eleves);
        StageAdapter = new ListeStageAdapter(this, listeStages);
        recyclerView.setAdapter(StageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        new ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(recyclerView);

        StageAdapter.setOnItemClickListener(new ListeStageAdapter.OnItemClickListener() {
            @Override
            public void OnDrapeauClick(int position, ImageView DrapeauView) {
                changerPrioriteStage(position, DrapeauView);
                StageAdapter.notifyDataSetChanged();
            }

            @Override
            public void OnImageEleveClick(int position) {

            }
        });
    }

    private void changerPrioriteStage(int positionStage, ImageView drapeauView) {
        Stage stage = listeStages.get(positionStage);
        int prioriteActuel = stage.getPriorite().ordinal();
        int prochainePriorite = prioriteActuel + 1;
        Priorite[] priorites = Priorite.values();
        prochainePriorite %= priorites.length;
        stage.setPriorite(priorites[prochainePriorite]);
        int couleur = ListeStageAdapter.renvoyerCouleur(stage.getPriorite());
        drapeauView.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), couleur));
        StageAdapter.notifyItemChanged(positionStage);
        dbHelper.changerPrioriteStage(stage);
    }


    public void lancerActiviteAjoutStage(View view) {
        Intent intent = new Intent(this, DemandeInfoEleve.class);
        startActivity(intent);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int removedItemIndex = viewHolder.getAdapterPosition();
            dbHelper.deleteStage(listeStages.get(removedItemIndex));
            listeStages.remove(removedItemIndex);
            StageAdapter.notifyItemRemoved(removedItemIndex);
        }
    };
}
