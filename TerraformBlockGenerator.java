
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class TerraformBlockGenerator {
    private Stack<String> tabs = new Stack<>();

    private String getTabs(){
        String tabStr = "";
        for(String tab: tabs){
            tabStr = tabStr + tab;
        }
        return tabStr;
    }

    private String serializeArray(Object val){
        String valString = "[";
        if(val instanceof String[]){
            String[] valArr = (String[]) val;
            for(String arrEl:valArr){
                if(arrEl.startsWith("#exp")){
                    arrEl.replace("#exp", "");
                    valString = valString + String.format("%s, ", arrEl);
                }else{
                    valString = valString + String.format("\"%s\",", arrEl);
                }
            }
        }else{
            Object[] objArr = (Object[]) val;
            for(Object obj: objArr){
                valString = valString + String.format("%s, ",obj);
            }
        }
        return valString.substring(0, valString.length()-2)+"]"; //remove the last ", "
    }

    private String serializeMap(Map<String, Object> map){
        Set<Map.Entry<String, Object>> tfAttrPairs = map.entrySet();
        String strPairs = "";
        for(Map.Entry<String, Object> tfAttr: tfAttrPairs){
            Object val = tfAttr.getValue();
            if(val instanceof String){
                String valString = (String) val;
                if(valString.startsWith("#exp")){
                    //value is an expression
                    valString = valString.replace("#exp", "");
                    strPairs = strPairs + String.format(getTabs() + "%s = %s\n", tfAttr.getKey(), valString);
                } else{
                    strPairs = strPairs + String.format(getTabs()+"%s = \"%s\"\n", tfAttr.getKey(), valString);
                }
            }else if(val instanceof Object[]){
                strPairs = strPairs + String.format(getTabs()+"%s = %s\n", tfAttr.getKey(), serializeArray(val));
            } else if (val instanceof Map){
                strPairs = strPairs + String.format(getTabs()+"%s {\n", tfAttr.getKey());
                tabs.push("\t");
                strPairs = strPairs + serializeMap((Map<String, Object>) val);
                tabs.pop();
                strPairs = strPairs + getTabs() +"}\n";
            }else{
                strPairs = strPairs + String.format(getTabs()+"%s = %s\n", tfAttr.getKey(), val);
            }
        }
        return strPairs;
    }

    public String generate(String blockType, String blockLabel1, String blockLabel2, Map<String, Object> attributes){
        String tfBlock = "";
        if(blockLabel2.isEmpty()){
            tfBlock = String.format("%s \"%s\" {\n", blockType, blockLabel1);
        }else{
            tfBlock = String.format("%s \"%s\" \"%s\" {\n", blockType, blockLabel1, blockLabel2);
        }
        tabs.push("\t");
        tfBlock = tfBlock + serializeMap(attributes);
        return tfBlock+"}";
    }
}