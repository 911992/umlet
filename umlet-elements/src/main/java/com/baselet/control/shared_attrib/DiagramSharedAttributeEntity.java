package com.baselet.control.shared_attrib;

import java.util.ArrayList;
import java.util.List;

//a one-to-many relation between a attrib name and attribs(lines)
public class DiagramSharedAttributeEntity {

    //when null, then it means the style should be applied globally
    private String style_name;
    private final ArrayList<String> attribs = new ArrayList<>(5);

    //clears all associated key-p
    public void invalide() {
        attribs.clear();
    }

    public String getStyle_name() {
        return style_name;
    }

    public void addAttrib(String arg_val) {
        this.attribs.add(arg_val);
    }

    public void setStyle_name(String style_name) {
        this.style_name = style_name;
    }

    public ArrayList<String> getAttribs() {
        return attribs;
    }

    public int line_stream(List<String> arg_strb, int arg_idx) {
        for (String _str : attribs) {
            arg_strb.add(arg_idx, _str);
            arg_idx++;
        }
        return attribs.size();
    }

    public int line_stream(List<String> arg_strb) {
        return line_stream(arg_strb, arg_strb.size());
    }

    public boolean has_anything() {
        return this.attribs.size() > 0;
    }

    public boolean name_equal(String arg_style_name) {
        return this.style_name.equals(arg_style_name);
    }

    public boolean merge(DiagramSharedAttributeEntity arg_entry) {
        if (arg_entry.getStyle_name().equals(this.getStyle_name()) /*&& arg_entry.has_anything()*/) {
            this.attribs.addAll(arg_entry.getAttribs());
            return true;
        }
        return false;
    }

}
