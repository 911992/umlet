package com.baselet.control.shared_attrib;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class handles diagram shared attributes are set in helptext.(when no element is selected, the welcome text).
 * Could be related to #515
 */
public class DiagramSharedAttributesHandler {
    private DiagramSharedAttributesHandler() {
        
    }
    
    //start of text for attrib definition
    private static final String ATTRIB_DEF_SOT = "#";
    
    //splittor for attrib names
    private static final String ATTRIB_SPLITTERS_REGEX = "\\,";
    
    private static final String ATTRIB_DEF_SOT_ESCAPE = "\\"+ATTRIB_DEF_SOT;
    
    //triggers for attribute replace
    public static final String ATTRIBUTE_MARKER = "#include=";
    
    public static final String ATTRIBUTE_MARKER_ESCAPE = "\\"+ATTRIBUTE_MARKER;
    
    //let say attrib0,attrib1,... that(,)
    public static final String ATTRIBUTE_MARKER_SPLITER = ",";
    
    private static final String ATTRIBUTE_MARKER_SPLITER_REGEX = "\\"+ATTRIBUTE_MARKER_SPLITER;
    
    //delim as the first line
    private static final String ATTRIBS_SOT_FL = "[includes]\n";
    
    //start of styling (at the middle)
    private static final String ATTRIBS_SOT = "\n"+ATTRIBS_SOT_FL;
    
    //atrrib definition, could be started by something for compatibility sake
    //for compatibility only, not essential
    private static final String ATTRIB_ATTRIB_DEF_SOT = "-";
    
    //escaping for ATTRIB_ATTRIB_DEF_SOT
    private static final String ATTRIB_ATTRIB_DEF_SOT_ESCAPED = "\\"+ATTRIB_ATTRIB_DEF_SOT;
    
    //contains only styling section as cached
    private static volatile String last_attrib_plain_str;
    
    //contains only styling section as cached
    private static volatile String last_attrib_proceed_str="";
    
    //contains non-styling section as cached
//    private static volatile String last_non_attrib_plain_str;
    
    //the last known (contains both attribs, and meta) original text were proceed
    private static volatile String last_proceesed_str;
    
    private static final DiagramSharedAttributeEntity public_attrib= new DiagramSharedAttributeEntity();
    
    //contains only attribs with atleast one def
    private static final ArrayList<DiagramSharedAttributeEntity> last_proceed_result=new ArrayList<>(5);
    
    public static final boolean has_any_includes(){
        return public_attrib.has_anything() || last_proceed_result.size()>0;
    }
    
    //returns numbers of lines has been added to the element property text
    public static final int process_property_text(List<String> arg_src,List<String> arg_dest){
        int _idx =0;
        if(public_attrib.has_anything()){
            _idx = public_attrib.line_stream(arg_dest);
        }
        String _markers[];        
        DiagramSharedAttributeEntity _att;
        for(String _tok : arg_src){
            if(_tok.startsWith(ATTRIBUTE_MARKER)){
                _markers = _tok.substring(ATTRIBUTE_MARKER.length()).split(ATTRIBUTE_MARKER_SPLITER_REGEX);
                for(String _m : _markers){
                    _att = find_attrib(_m, last_proceed_result);
                    if(_att == null){
                        continue;
                    }
                    _idx = _idx + _att.line_stream(arg_dest);
                }
            }
            else {
                if(_tok.startsWith(ATTRIBUTE_MARKER_ESCAPE)){
                    _tok = _tok.substring(1);
                }
                arg_dest.add(_tok);
            }
        };
        return _idx;
    }
    
    //split the non--attrib and attrib sections
    private static final void split_attrib(String arg_txt){
        int _len = ATTRIBS_SOT.length();
        int _idx = arg_txt.indexOf(ATTRIBS_SOT);
        if(_idx==-1){
            _len =ATTRIBS_SOT_FL.length();
            _idx = arg_txt.startsWith(ATTRIBS_SOT_FL)?0:-1;
        }
        if(_idx==-1){//no stylying
//            last_non_attrib_plain_str = arg_txt;
            last_attrib_plain_str = "";
            return;
        }
//        last_non_attrib_plain_str = arg_txt.substring(0,_idx);
        last_attrib_plain_str = arg_txt.substring(_idx+_len);
        
    }
    
    //processes the given help text, and return the new string value where has non-styling data related
    public static void process_text(String arg_data){
        //using the cache, as happens most of the time
        if(last_proceesed_str!=null && last_proceesed_str.equals(arg_data)){
            return;
        }
        last_proceesed_str = arg_data;
        split_attrib(arg_data);//will split the text and shared styling in two strings
        
        process_attrib_text();
//        return;
    }
    
    private static final DiagramSharedAttributeEntity find_attrib(String arg_name,ArrayList<DiagramSharedAttributeEntity> arg_ctx){
        for(DiagramSharedAttributeEntity _ins : arg_ctx){
            if(_ins.name_equal(arg_name)){
                return _ins;
            }
        }
        return null;
    }
    
    private static final DiagramSharedAttributeEntity find_or_create_attrib(String arg_name,ArrayList<DiagramSharedAttributeEntity> arg_ctx){
        DiagramSharedAttributeEntity _res = find_attrib(arg_name, arg_ctx);
        if(_res!=null){
            return _res;
        }
        _res = new DiagramSharedAttributeEntity();
        _res.setStyle_name(arg_name);
        arg_ctx.add(_res);
        return _res;
    }
    
    private static final void merge_or_add_attribs(ArrayList<DiagramSharedAttributeEntity> arg_dest,ArrayList<DiagramSharedAttributeEntity> arg_src){
        if(arg_src.size() == 0){
            return;
        }
        for(DiagramSharedAttributeEntity _dest:arg_dest){
            DiagramSharedAttributeEntity _src;
            for(int a=0;a<arg_src.size();a++){
                _src = arg_src.get(a);
                if(_dest.merge(_src)){
                    arg_src.remove(a);
                    a = arg_src.size();
                }
            }
        }
        arg_dest.addAll(arg_src);
        arg_src.clear();
    }
    
    private static void process_attrib_text(){
        //no any changes on stylying
        if(last_attrib_proceed_str.equals(last_attrib_plain_str) ){
            return;
        }   
        public_attrib.invalide();
        last_proceed_result.clear();
        last_attrib_proceed_str = last_attrib_plain_str;
        if(last_attrib_proceed_str.length() == 0){
            return;
        }
        boolean public_processing=true;
        
        StringTokenizer _str_reader=new StringTokenizer(last_attrib_plain_str, "\n");
        String line;
        String[] attrib_name=null;
        while(_str_reader.hasMoreTokens()){
            line=_str_reader.nextToken();
            if(line.startsWith(ATTRIB_DEF_SOT)){
                public_processing = false;
                //splitting the attrib names
                attrib_name = line.substring(1).split(ATTRIB_SPLITTERS_REGEX);
                continue;
            }
            //escaped SOT for attribs, or an attrib
            if(line.startsWith(ATTRIB_ATTRIB_DEF_SOT) || line.startsWith(ATTRIB_ATTRIB_DEF_SOT_ESCAPED) || line.startsWith(ATTRIB_DEF_SOT_ESCAPE)){
                line = line.substring(1);
            }
            if(public_processing){
                public_attrib.addAttrib(line);
            }else{
                for(String _st : attrib_name){
                    if(_st.trim().isEmpty()){
                        continue;
                    }
                    find_or_create_attrib(_st, last_proceed_result).addAttrib(line);
                }
            }
        }
        
    }
}
