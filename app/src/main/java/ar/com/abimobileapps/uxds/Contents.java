package ar.com.abimobileapps.uxds;

import android.content.res.AssetManager;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing contents for user interface
 */
public class Contents {

    /**
     * An array of items.
     */
    public List<ContentsItem> ITEMS = new ArrayList<ContentsItem>();

    /**
     * A map of items, by ID.
     */
    public Map<String, ContentsItem> ITEM_MAP = new HashMap<String, ContentsItem>();

    public Contents(AssetManager assetManager)
    {
        XmlPullParser parser = Xml.newPullParser();

        try {
            InputStream is = assetManager.open("items.xml");
            parser.setInput(is, "UTF-8");

            for (int eventType = parser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = parser.next()) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equalsIgnoreCase("item")) {
                            ContentsItem item = new ContentsItem(parser.getAttributeValue(0), parser.getAttributeValue(1));
                            addItem(item);
                        }
                        break;

                    default:
                        break;
                }
            }
        }
        catch (IOException e) {
            clear();
        }
        catch (XmlPullParserException e) {
            clear();
        }

    }

    private void addItem(ContentsItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private void clear() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    /**
     * An item representing a piece of contents.
     */
    public class ContentsItem {
        public String id;
        public String contents;

        public ContentsItem(String id, String contents) {
            this.id = id;
            this.contents = contents;
        }

        @Override
        public String toString() {
            return contents;
        }
    }

}
