package nds.util.ejb;

public class SequenceBeanCMP extends SequenceBean {
    public Integer curvalue;
    public String name;
    public Integer getCurvalue() {
        return curvalue;
    }
    public void setCurvalue(Integer curvalue) {
        this.curvalue = curvalue;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}