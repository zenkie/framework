package nds.schema;

import nds.util.StringBufferWriter;

public class AliasTable {
    String name;
    String realTableName;
    String condition;

    public AliasTable() {}
    public void setName(String name) {
        this.name=name;
    }
    public String getName(){
    	return name;
    }
    public void setRealTableName(String tn) {
        this.realTableName=tn;
    }
    public String getRealTableName(){
    	return realTableName;
    }
    public void setCondition(String cn) {
        this.condition=cn;
    }
    public String getCondition(){
    	return condition;
    }
    public void printXML(StringBufferWriter b){
        b.println("<aliasTable>");
        b.pushIndent();
        printNode(b,"name", name);
        printNode(b,"real-table", realTableName);
        printNode(b,"condition", condition);
        b.popIndent();
        b.println("</aliasTable>");
    }
    private void printNode(StringBufferWriter b, String name, Object value){
        b.println("<"+name+">"+ value+"</"+name+">");
    }
}
