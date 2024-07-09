

/*
* Copyright 2024 Iradukunda Moustapa

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
* 
* */

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