package ca.qc.bdeb.c5gm.stageplanif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;

import java.io.ByteArrayOutputStream;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ca.qc.bdeb.c5gm.stageplanif.data.Priorite;
import ca.qc.bdeb.c5gm.stageplanif.data.Stage;
import ca.qc.bdeb.c5gm.stageplanif.data.Stockage;

public class Utils {
    public static final HashMap<DayOfWeek, String> JOURS_DE_LA_SEMAINE = new HashMap<DayOfWeek, String>() {{
        put(DayOfWeek.SUNDAY, "Dimanche");
        put(DayOfWeek.MONDAY, "Lundi");
        put(DayOfWeek.TUESDAY, "Mardi");
        put(DayOfWeek.WEDNESDAY, "Mercredi");
        put(DayOfWeek.THURSDAY, "Jeudi");
        put(DayOfWeek.FRIDAY, "Vendredi");
        put(DayOfWeek.SATURDAY, "Samedi");
    }};

    public static Context context;

    public static ArrayList<Integer> calculerPrioritesSelectionnees(int selection) {
        ArrayList<Integer> ListePrioritesSelectionnees = new ArrayList<>();

        for (Priorite p : Priorite.values()) {
            if ((selection & p.getValeur()) > 0) {
                ListePrioritesSelectionnees.add(p.getValeur());
            }
        }
        return ListePrioritesSelectionnees;
    }

    public static ArrayList<Stage> filtrerListeStages(ArrayList<Integer> ListePrioritesSelectionnees, ArrayList<Stage> listeStages) {
        ArrayList<Stage> listeStagesMasques = new ArrayList<>();

        for (Stage s : listeStages) {
            if (!ListePrioritesSelectionnees.contains(s.getPriorite().getValeur())) {
                listeStagesMasques.add(s);
            }
        }
        return listeStagesMasques;
    }

    /**
     * Renvoie une couleur en fonction de la priorite passée en paramètre
     * Priorité minimum = vert, Priorité moyenne = jaune, Priorité maximum = Rouge
     * Sinon renvoie du noir
     * @param priorite
     * @return la couleur en format RGB (int)
     */
    public static int renvoyerCouleur(Priorite priorite) {
        switch (priorite) {
            case BASSE:
                return R.color.green;
            case MOYENNE:
                return R.color.yellow;
            case HAUTE:
                return R.color.red;
            default:
                return R.color.black;
        }
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static Bitmap getImageAjustee(byte[] photo, int targetW, int targetH) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        BitmapFactory.decodeByteArray(photo, 0, photo.length, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length, bmOptions);
        return bitmap;
    }

    /**
     * Methode qui determine l'annee scolaire en cours
     * @return l'annee scolaire en cours
     */
    public static String getAnneeScolaire() {
        Calendar calendrier = Calendar.getInstance();
        int anneeActuelle = calendrier.get(Calendar.YEAR);
        int moisActuel = calendrier.get(Calendar.MONTH);
        if (moisActuel < 7) {
            return String.format("%d-%d", anneeActuelle - 1, anneeActuelle);
        }
        return String.format("%d-%d", anneeActuelle, anneeActuelle + 1);
    }

    public static <K, V> K trouverCleAvecValeurHashMap(HashMap<K, V> map, V value) {
        for (Map.Entry<K, V> entree : map.entrySet()) {
            if (Objects.equals(value, entree.getValue())) {
                return entree.getKey();
            }
        }
        return null;
    }

    public static <K, V> List<V> creeListeAvecValeursHashMap (HashMap<K, V> map){
        return map.values().stream().collect(Collectors.toList());
    }

    public static String formatterDateTime(LocalDateTime temps) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        return temps.format(formatter);
    }
}
