package ca.qc.bdeb.c5gm.stageplanif.reseau;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ca.qc.bdeb.c5gm.stageplanif.ConnectUtils;
import ca.qc.bdeb.c5gm.stageplanif.Utils;
import ca.qc.bdeb.c5gm.stageplanif.data.Priorite;
import ca.qc.bdeb.c5gm.stageplanif.data.Stockage;
import ca.qc.bdeb.c5gm.stageplanif.data.TypeCompte;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConnexionBD {
    private static IAPI client = APIClient.getRetrofit().create(IAPI.class);
    private static JSONArray entreprises;

    public static void updateEntreprises() {
        client.getEntreprises(ConnectUtils.authToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("justine_tag", response.toString());
                try {
                    if (response.code() == 200) {
                        Stockage dbHelper = Stockage.getInstance(Utils.context);
                        entreprises = new JSONArray(response.body().string());
                        for (int i = 0; i < entreprises.length(); i++) {
                            JSONObject entreprise = entreprises.getJSONObject(i);
                            String id = entreprise.get("id").toString();
                            String nom = entreprise.getString("nom");
                            String adresse = entreprise.getString("adresse");
                            String ville = entreprise.getString("ville");
                            String province = entreprise.getString("province");
                            String codePostal = entreprise.getString("codePostal");
                            dbHelper.ajouterOumodifierEntreprise(id, nom, adresse, ville, province, codePostal);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }

    public static void updateComptesEleves() {
        client.getComptesEleves(ConnectUtils.authToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("justine_tag", response.toString());
                try {
                    if (response.code() == 200) {
                        JSONArray comptes = new JSONArray(response.body().string());
                        for (int i = 0; i < comptes.length(); i++) {
                            JSONObject compte = comptes.getJSONObject(i);
                            updateCompte(compte);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }

    public static void updateCompte(JSONObject compte) {
        try {
            Stockage dbHelper = Stockage.getInstance(Utils.context);
            String id = compte.getString("id");
            String nom = compte.getString("nom");
            String prenom = compte.getString("prenom");
            String email = compte.getString("email");
            String typeCompte = compte.getString("type_compte");
            int typeCompteId = TypeCompte.valueOf(typeCompte).getValeur();
            dbHelper.ajouterOuModifierCompte(id, nom, prenom, email, typeCompteId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void test() {
        client.getStages(ConnectUtils.authToken).isExecuted();
    }
    public static void updateStages() {
        client.getStages(ConnectUtils.authToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i("justine_tag", response.toString());
                try {
                    if (response.code() == 200) {
                        Stockage dbHelper = Stockage.getInstance(Utils.context);
                        JSONArray stages = new JSONArray(response.body().string());
                        for (int i = 0; i < stages.length(); i++) {
                            JSONObject stage = stages.getJSONObject(i);
                            String id = stage.getString("id");
                            String deletedAt = stage.getString("deletedAt");
                            String anneeScolaire = stage.getString("anneeScolaire");
                            JSONObject jsonEtudiant = stage.getJSONObject("etudiant");
                            String etudiantId = jsonEtudiant.getString("id");
                            JSONObject professeur = stage.getJSONObject("professeur");
                            String professeurId = professeur.getString("id");
                            JSONObject entreprise = stage.getJSONObject("entreprise");
                            String entrepriseId = entreprise.getString("id");
                            String prioriteStr = stage.getString("priorite");
                            int priorite = Priorite.valueOf(prioriteStr).getValeur();
                            String commentaire = stage.getString("commentaire");
                            if(commentaire == "null") {
                                commentaire = null;
                            }
                            String heureDebutStr = stage.getString("heureDebut");
                            String heureFinStr = stage.getString("heureFin");
                            String heureDebutPauseStr = stage.getString("heureDebutPause");
                            String heureFinPauseStr = stage.getString("heureFinPause");
                            int heureDebut = -1;
                            int heureFin = -1;
                            int heureDebutPause = -1;
                            int heureFinPause = -1;
                            try{
                                if(isNumeric(heureDebutStr)) {
                                     heureDebut = Integer.parseInt(heureDebutStr);
                                }
                                if(isNumeric(heureFinStr)) {
                                    heureFin = Integer.parseInt(heureFinStr);
                                }
                                if(isNumeric(heureDebutPauseStr)) {
                                    heureDebutPause = Integer.parseInt(heureDebutPauseStr);
                                }
                                if(isNumeric(heureFinPauseStr)) {
                                    heureFinPause = Integer.parseInt(heureFinPauseStr);
                                }
                            }
                            catch (NumberFormatException ex){
                                ex.printStackTrace();
                            }

                            if(deletedAt == "null") {
                                dbHelper.ajouterouModifierStage(id, anneeScolaire,entrepriseId, etudiantId,
                                        professeurId, commentaire, heureDebut, heureFin, priorite,
                                        heureDebutPause, heureFinPause);
                            } else {
                                if(dbHelper.stageExists(id)) {
                                    dbHelper.deleteStage(id);
                                }
                            }
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            private boolean isNumeric(String str){
                return str != null && str.matches("[0-9.]+");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", t.toString());
            }
        });
    }

}