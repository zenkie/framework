package nds.net.test;

public class UpateActivePOSTest {
    private boolean isLimitToSpecialUpdateClients= false;
    private String[] specialUpdateClients=null;
    private String[] specialNoUpdateClients=null;
    public UpateActivePOSTest() {
        isLimitToSpecialUpdateClients=true;
        specialUpdateClients=new String[]{"*"};
        specialNoUpdateClients=new String[]{"CSJ005","CSJ007","CSJ029","COJ001","ZZZ001"};
        System.out.print(checkShouldNotifyLatestVersion("CMA023"));
        System.out.print(checkShouldNotifyLatestVersion("CSJ029"));
    }
    public static void main(String[] args) {
        UpateActivePOSTest upateActivePOSTest1 = new UpateActivePOSTest();
    }
    private boolean checkShouldNotifyLatestVersion(String clientName){
        if(isLimitToSpecialUpdateClients){
            if(clientName==null) return false;
            if(specialUpdateClients !=null){
                for(int i=0;i< specialUpdateClients.length; i++){
                    if(clientName.equalsIgnoreCase(specialUpdateClients[i])) return true;
                    if("*".equalsIgnoreCase(specialUpdateClients[i])){
                        // check for no update clients, if contained, return false;
                        if(this.specialNoUpdateClients !=null){
                            for(int j=0;j< specialNoUpdateClients.length;j++){
                                if(clientName.equalsIgnoreCase(specialNoUpdateClients[j])) return false;
                            }
                        }
                        return true; // all clients will be notified
                    }// end *
                }
            }
            return false;
        }else{
            return true;
        }
    }

}