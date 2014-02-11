package mp.agencja.apsik.kidotv.main;

import java.util.ArrayList;
import java.util.HashMap;

class DefaultPlayLists {

    public static ArrayList<HashMap<String, String>> getDefaultPlayLists() {
        final ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(6);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("title", "Bajki 1 Bajki 1 Bajki 1 Bajki 1 Bajki 1 Bajki 1 Bajki 1");
        map.put("play_list_id", "PL965BC7993FC1045E");
        map.put("is_favorite", "false");
        list.add(map);

        map = new HashMap<String, String>();
        map.put("title", "Bajki 2");
        map.put("play_list_id", "PL5QgUol9DKoK9KbVYJK1R8O5cq8J7cRN4");
        map.put("is_favorite", "false");
        list.add(map);

        map = new HashMap<String, String>();
        map.put("title", "Bajki 3");
        map.put("play_list_id", "PLSSbFe1zKBe-4RNVeJ-zTwgUFP8YJSA7u");
        map.put("is_favorite", "false");
        list.add(map);

        map = new HashMap<String, String>();
        map.put("title", "Bajki 4");
        map.put("play_list_id", "PLTYxQpbS36KYUgwKP2o_mrQonPxIBRkra");
        map.put("is_favorite", "false");
        list.add(map);

        map = new HashMap<String, String>();
        map.put("title", "Bajki 5");
        map.put("play_list_id", "PL6CE26AD0E949E015");
        map.put("is_favorite", "false");
        list.add(map);

        map = new HashMap<String, String>();
        map.put("title", "Bajki 6");
        map.put("play_list_id", "PL352939F97C698EC3");
        map.put("is_favorite", "false");
        list.add(map);
        return list;
    }


}
