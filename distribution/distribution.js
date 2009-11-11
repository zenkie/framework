var dist=null;
var DIST=Class.create();
DIST.prototype={
    initialize: function() {
        this.itemStr="";
        this.manuStr="";
        this.allot_id=null;
        this.manu=null;
        this.item=null;
        this.product=new Array();
        this.status=0;
        dwr.util.useLoadingMessage(gMessageHolder.LOADING);
        dwr.util.setEscapeHtml(false);
        /** A function to call if something fails. */
        dwr.engine._errorHandler =  function(message, ex) {
            while(ex!=null && ex.cause!=null) ex=ex.cause;
            if(ex!=null)message=ex.message;// dwr.engine._debug("Error: " + ex.name + "," + ex.message+","+ ex.cause.message, true);
            if (message == null || message == "") alert("A server error has occured. More information may be available in the console.");
            else if (message.indexOf("0x80040111") != -1) dwr.engine._debug(message);
            else alert(message);
        };
        application.addEventListener( "DO_QUERY", this._onLoadMetrix, this);
        application.addEventListener("FUND_BALANCE",this._onfundQuery,this);
        application.addEventListener("DO_SAVE",this._onsaveDate,this);
        application.addEventListener("RELOAD",this._onreShow,this);
    },
    queryObject: function(style){
        var evt={};
        evt.command="DBJSONXML";
        evt.callbackEvent="DO_QUERY";
        var load_type=$("load_type").value;
        var reg=/^\d{8}$/;
        var m_allot_id=$("fund_balance").value||"-1";
        if(style&&style=='doc'){
            if(!$('column_26996').value){
                alert("单据号不能为空！");
                return;
            }
           var searchord=$('column_26996').value;
           var param={"or_type":"","c_dest":"","c_orig":"","m_product":"",
                "datest":"","datend":"","load_type":load_type,"m_allot_id":m_allot_id,"searchord":searchord};
        }else{
            var doctype=$("column_26991").value;
            if(!doctype){
                alert("订单类型不能为空！");
                return;
            }
            var orig_out_fk=$("fk_column_26992").value;
            if(!orig_out_fk){
                alert("发货店仓不能为空！");
                return;
            }
            if(!$("column_26993").value){
                alert("收货店仓不能为空！");
                return;
            }
            var orig_in_sql=$("column_26993").value;

            if(!$("column_26994").value){
                alert("款号不能为空！");
                return;
            }
            var product_filter=$("column_26994").value;
            var billdatebeg=$("column_26995").value.strip();
            var year=billdatebeg.substring(0,4);
            var month=billdatebeg.substring(4,6);
            var date=billdatebeg.substring(6,8);
            var beg=month+"/"+date+"/"+year;
            if(!this.checkIsDate(month,date,year)||!reg.test(billdatebeg)){
                alert("开始日期格式不对！请输入8位有效数字。");
                return;
            }
            var billdateend=$("column_269966").value.strip();
            var year1=billdateend.substring(0,4);
            var month1=billdateend.substring(4,6);
            var date1=billdateend.substring(6,8);
            var end=month1+"/"+date1+"/"+year1;
            if(!this.checkIsDate(month1,date1,year1)||!reg.test(billdateend)){
                alert("结束日期格式不对！请输入8位有效数字。");
                return;
            }
            var param={"or_type":doctype,"c_dest":orig_in_sql,"c_orig":orig_out_fk,"m_product":product_filter,
                "datest":billdatebeg,"datend":billdateend,"load_type":load_type,"m_allot_id":m_allot_id,"searchord":""};
        }
        evt.param=Object.toJSON(param);
        evt.table="m_allot";
        evt.action="distribution";
        evt.permission="r";
        this._executeCommandEvent(evt);
    },
    saveDate:function(type){
        if($("orderStatus").value=="2"){
            alert("该单据已提交，不可再进行操作！");
            return;
        }
        var evt={};
        evt.command="DBJSONXML";
        evt.callbackEvent="DO_SAVE";
        if(type=='ord'){
            if(!confirm("单据生成不可修改！确认生成单据？")){
                return;
            }
        }
        var m_allot_id=$("fund_balance").value||"null";
        var m_item=new Array();
        var inputItems=jQuery("#ph-from-right-table table input[title][value!='']");
        for(var i=0;i<inputItems.length;i++){
            var ii={};
            if(!isNaN(inputItems[i].value)){
                ii.qty_ady=inputItems[i].value;
                ii.m_product_alias_id=inputItems[i].title;
                ii.docno = inputItems[i].name;
                m_item.push(ii);
            }
        }
        var param={};
        param.type=type;
        param.m_allot_id=m_allot_id;
        param.m_item=(m_item.length==0?"null":m_item);
        evt.param=Object.toJSON(param);
        evt.table="m_allot";
        evt.action="save";
        evt.permission="r";
        this._executeCommandEvent(evt);
    },
    reShow:function(){
        var evt={};
        evt.command="DBJSONXML";
        evt.callbackEvent="RELOAD";
        var m_allot_id=$("fund_balance").value||"-1";
        var param={"or_type":"-1","c_dest":"-1","c_orig":"-1","m_product":"-1",
            "datest":"-1","datend":"-1","load_type":"reload","m_allot_id":m_allot_id};
        evt.param=Object.toJSON(param);
        evt.table="m_allot";
        evt.action="distribution";
        evt.permission="r";
        this._executeCommandEvent(evt);
    },
    _onreShow:function(e){
        var data=e.getUserData();
        var ret=data.jsonResult.evalJSON();
        $("column_26992").value=ret.C_ORIG;
        $("column_26993_fd").value=ret.DEST_FILTER||"(可用 = Y)";
        $("column_26994_fd").value=ret.Product_Filter;
        $("column_26995").value=ret.Billdatebeg;
        $("column_269966").value=ret.Billdateend;
        var isArray=ret.isarray;
        var status=ret.status;
        $("orderStatus").value=status;
        if(status=="2"){
           $("submitImge").style.display=""; 
        }
        this._onLoadMetrix(e);
    },
    _onsaveDate:function(e){
        var data=e.getUserData();
        var ret=data.jsonResult.evalJSON();
        if(ret.data=="OK"){
            this.status=0;
            jQuery("#ph-serach-bg>div input[type='image']").hide();
            alert("保存成功！");
            $("isChanged").value="false";
            
        }else if(ret.data=="YES"){
            this.status=0;
            alert("提交成功！");
            $("isChanged").value="false";
            self.close();
        }else{
            alert("出现错误！可能原因："+ret.data);
        }
    },
    //经销商资金余额
    fundQuery:function(){
        var evt={};
        evt.command="DBJSONXML";
        evt.callbackEvent="FUND_BALANCE";
        var w=window.parent;
        if(!w)w=window.opener;
        var m_allot_id=w.document.getElementById("fund_balance").value||"-1";
        var param={"m_allot_id":m_allot_id};
        evt.param=Object.toJSON(param);
        evt.table="m_allot";
        evt.action = "cus";
        evt.permission="r";
        this._executeCommandEvent(evt);
    },
    _onfundQuery:function(e){
        dwr.util.useLoadingMessage(gMessageHolder.LOADING);
        var data=e.getUserData();
        var ret=data.jsonResult.evalJSON();
        var fundStr= "<table  width=\"700\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\" bordercolor=\"#8db6d9\" bordercolorlight=\"#FFFFFF\" bordercolordark=\"#FFFFFF\" bgcolor=\"#8db6d9\" class=\"modify_table\" align=\"center\">"+
                     "<tr><td width=\"70\" bgcolor=\"#8db6d9\" class=\"table-title-bg\"><div class=\"td-title\">序号</div></td>"+
                     "<td width=\"90\" bgcolor=\"#8db6d9\" class=\"table-title-bg\"><div class=\"td-title\">经销商</div></td>"+
                     "<td width=\"80\" bgcolor=\"#8db6d9\" class=\"table-title-bg\"><div class=\"td-title\">资金余额</div></td>"+
                     "<td width=\"90\" bgcolor=\"#8db6d9\" class=\"table-title-bg\"><div class=\"td-title\">已占用金额</div></td>"+
                     "<td width=\"100\" bgcolor=\"#8db6d9\" class=\"table-title-bg\"><div class=\"td-title\">配货信用下限</div></td>"+
                     "<td width=\"90\" bgcolor=\"#8db6d9\" class=\"table-title-bg\"><div class=\"td-title\">可用金额</div></td>"+
                     "<td width=\"90\" bgcolor=\"#8db6d9\" class=\"table-title-bg\"><div class=\"td-title\">本次配货金额</div></td>"+
                     "<td width=\"90\" bgcolor=\"#8db6d9\" class=\"table-title-bg\"><div class=\"td-title\">剩余金额</div></td>"+
                     "</tr>";
        if(ret.data=="null"){
            fundStr="<div style='font-size:20px;color:red;text-align:center;font-weight:bold;vertical-align:middle'>您没有选择经销商！</div>";
        }else{
            var funditem=ret.data;
            if(this.checkIsArray(funditem)){
                for(var i=0;i<funditem.length;i++){
                    fundStr+="<tr>"+
                             "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+(i+1)+"</div></td>"+
                             "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+funditem[i].facusitem.NAME||""+"</div></td>"+
                             "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+funditem[i].facusitem.FEEREMAIN||0+"</div></td>"+
                             "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+funditem[i].facusitem.FEECHECKED||0+"</div></td>"+
                             "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+funditem[i].facusitem.FEELTAKE||0+"</div></td>"+
                             "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+funditem[i].facusitem.FEECANTAKE||0+"</div></td>"+
                             "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+funditem[i].facusitem.FEEALLOT||0+"</div></td>"+
                             "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+funditem[i].facusitem.FEEREM||0+"</div></td>"+
                             " </tr>";
                }
            }
            else{
                fundStr+="<tr>"+
                         "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+1+"</div></td>"+
                         "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+(funditem.facusitem.NAME||"")+"</div></td>"+
                         "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+(funditem.facusitem.FEEREMAIN||0)+"</div></td>"+
                         "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+(funditem.facusitem.FEECHECKED||0)+"</div></td>"+
                         "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+(funditem.facusitem.FEELTAKE||0)+"</div></td>"+
                         "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+(funditem.facusitem.FEECANTAKE||0)+"</div></td>"+
                         "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+(funditem.facusitem.FEEALLOT||0)+"</div></td>"+
                         "<td bgcolor=\"#8db6d9\" class=\"td-bg\"><div class=\"td-font\">"+(funditem.facusitem.FEEREM||0)+"</div></td>"+
                         " </tr>";
            }
            fundStr+="</table>";
        }
        $("fund_table1").innerHTML=fundStr;
    },
    /*switchModel:function(){
        if($("switchModel").value=="切换为矩阵模式"){
            if(confirm("切换模式会重新装载，为避免数据丢失，建议先进行保存！确认切换？")){
                $("switchModel").value="切换为列表模式";
                $("load_model").value="metrix";
                if($("load_type")=="load") {
                   dist.queryObject();
                }else{
                    dist.reShow();
                }
            }
        }else{
           if(confirm("切换模式会重新装载，为避免数据丢失，建议先进行保存！确认切换？")){
                $("switchModel").value="切换为矩阵模式";
                $("load_model").value="list";
                if($("load_type")=="load"){
                   dist.queryObject();
                }else{
                    dist.reShow();
                }
            }
        }
    },*/
    _onLoadMetrix:function(e){
        dwr.util.useLoadingMessage(gMessageHolder.LOADING);
        var data=e.getUserData();
        var ret=data.jsonResult.evalJSON();
        this.manuStr="";
        this.itemStr="";
        if(ret.data&&ret.data=="null"){
             $("ph-from-right-table").innerHTML="<div style='font-size:20px;color:red;text-align:center;font-weight:bold;vertical-align:middle'>没有数据！</div>";
            return;
        }
         $("isChanged").value='false';
        if(ret.searchord){
            $('Details').style.display='none';$('Documents').style.display='';
            $("column_26996_fd").value=ret.searchord;
            jQuery("#Documents>table input").attr("disabled","true");
            jQuery("#Documents>table img").css("display","none");
        }else{
            $('Details').style.display='';$('Documents').style.display='none';
        }
        var pdt=ret.data.m_product;
        var totCan=0;
        var totRem=0;
        if(this.checkIsArray(pdt)) {
            for(var ii=0;ii<pdt.length;ii++) {
                var ptotRem=0;
                var ptotCan=0;
                if(ii==0){
                    $("ph-pic-img-txt").innerHTML=pdt[ii].xmlns+" <br/>"+pdt[ii].value;
                    $("pdt-img").src = "/pdt/"+pdt[ii].M_PRODUCT_LIST+"_1_2.jpg";
                }
                this.manuStr+="\n<li><div class=\"txt-on\"  onclick='javascript:$(\"pdt-img\").src = \"/pdt/"+pdt[ii].M_PRODUCT_LIST+"_1_2.jpg\";" +
                              "$(\"ph-pic-img-txt\").innerHTML=\""+pdt[ii].xmlns+"<br/>"+pdt[ii].value+"\";" +
                              "dist.showContent1(\""+pdt[ii].xmlns+"\");" +
                              "this.style.backgroundColor=\"#8db6d9\"; this.style.color=\"white\";'"+
                              (ii==0?"  style='background:#8db6d9'":"")+">"+pdt[ii].xmlns+"</div></li>\n";
                var itemColor=pdt[ii].color;
                var colorArr=new Array();
                colorArr=this.forMetrixChangeToArr(itemColor);
                var sizeArr=colorArr[0].stores[0].docnos[0].tag.size;
                var tagLen=sizeArr.length;
                var item="";                        
                for(var p=0;p<colorArr.length;p++){
                    for(var pp=0;pp<colorArr[p].stores.length;pp++){
                        for(var ppp=0;ppp<colorArr[p].stores[pp].docnos.length;ppp++){
                            for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                totCan+=colorArr[p].stores[pp].docnos[ppp].tag.can[w]!='non'?parseInt(colorArr[p].stores[pp].docnos[ppp].tag.can[w]):0;
                                totRem+=colorArr[p].stores[pp].docnos[ppp].tag.rem[w]!='non'?parseInt(colorArr[p].stores[pp].docnos[ppp].tag.rem[w]):0;
                                ptotCan+=colorArr[p].stores[pp].docnos[ppp].tag.can[w]!='non'?parseInt(colorArr[p].stores[pp].docnos[ppp].tag.can[w]):0;
                                ptotRem+=colorArr[p].stores[pp].docnos[ppp].tag.rem[w]!='non'?parseInt(colorArr[p].stores[pp].docnos[ppp].tag.rem[w]):0;
                            }
                            for(var con=0;con<4;con++){
                                item+="<tr>";
                                if(pp==0&&ppp==0&&con==0){
                                    item+= "<td rowspan=\""+this.forColorSpan(colorArr[p])+"\" valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-left-txt\">"+colorArr[p].name+"</td>";
                                }
                                if(ppp==0&&con==0){
                                    item+="<td rowspan=\""+this.forStorSpan(colorArr[p].stores[pp])+"\" valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-left-txt01\">"+colorArr[p].stores[pp].name+"</td>"
                                }
                                if(con==0){
                                    item+="<td rowspan=\"4\" valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-bg\">"+colorArr[p].stores[pp].docnos[ppp].no+"</td>"+
                                                  "<td rowspan=\"4\" valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-bg\">"+this.forChangeDate(colorArr[p].stores[pp].docnos[ppp].date)+"</td>"+
                                                  "<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txt\">配货</td>";
                                    for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                        var itemMetrixTr=colorArr[p].stores[pp].docnos[ppp].tag.can[w];
                                        var barCode=colorArr[p].stores[pp].docnos[ppp].tag.barCode[w];
                                        var qtyAl=parseInt(colorArr[p].stores[pp].docnos[ppp].tag.qtyAl[w]);
                                        var docno=colorArr[p].stores[pp].docnos[ppp].no;
                                        qtyAl=isNaN(qtyAl)?0:qtyAl;
                                        item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-bg\""+(itemMetrixTr=='non'?" style=\"background-color:#eeeeee\"":"")+">"+(itemMetrixTr!='non'?"<input title='"+barCode+"' name=\""+docno+"\" type=\"text\" class=\"td-txt-input\" value=\""+(qtyAl==0?'':qtyAl)+"\"/>":"")+"</td>";
                                    }
                                }
                                if(con==1){
                                    item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txt\">可配</td>";
                                    for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                        var itemMetrixTr=colorArr[p].stores[pp].docnos[ppp].tag.can[w];
                                        item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txtK\""+(itemMetrixTr=='non'?" style=\"background-color:#eeeeee\"":"")+">"+(itemMetrixTr!='non'?itemMetrixTr:"")+"</td>";
                                    }
                                }
                                if(con==2){
                                    item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txt\">未配</td>";
                                    for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                        var itemMetrixTr=colorArr[p].stores[pp].docnos[ppp].tag.rem[w];
                                        item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txtW\""+(itemMetrixTr=='non'?" style=\"background-color:#eeeeee\"":"")+">"+(itemMetrixTr!='non'?itemMetrixTr:"")+"</td>";
                                    }
                                }
                                if(con==3){
                                    item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txt\">订单量</td>";
                                    for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                        var itemMetrixTr=colorArr[p].stores[pp].docnos[ppp].tag.dest[w];
                                        item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txtD\""+(itemMetrixTr=='non'?" style=\"background-color:#eeeeee\"":"")+">"+(itemMetrixTr!='non'?itemMetrixTr:"")+"</td>";
                                    }
                                }
                                item+="</tr>";
                            }
                        }
                    }
                }
                this.itemStr+="<table id='"+pdt[ii].xmlns+ "'  title=\""+ptotCan+":"+ptotRem+"\" border=\"0\" cellpadding=\"0\" cellspacing=\"1\"  bgcolor=\"#8db6d9\""+(ii!=0?" style='display:none;table-layout:fixed'":" style='table-layout:fixed'")+">\n";
                this.itemStr+=this.forTableShowStyle(tagLen)+"<tr>"+
                              "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">颜色</td>"+
                              "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">店仓</td>" +
                              "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">订单号</td>" +
                              "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">发货日期</td>"+
                              "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">尺寸</td>";
                for(var e=0;e<sizeArr.length;e++){
                    this.itemStr+="<td bgcolor=\"#B6D0E7\" class=\"td-right-title\">"+sizeArr[e]+"</td>";
                };
                this.itemStr+="</tr>";
                this.itemStr+=item;
                this.itemStr+="</table>"
                if(ii==0){
                    $("input-5").innerHTML=ptotCan;
                    $("input-4").innerHTML=ptotRem;
                }
                this.product[ii]={};
                this.product[ii].name=pdt[ii].xmlns;
                this.product[ii].value=pdt[ii].value;
                this.product[ii].colors=colorArr;
            }
        }else{
            $("ph-pic-img-txt").innerHTML=pdt.xmlns+" <br/>"+pdt.value;
            $("pdt-img").src = "/pdt/"+pdt.M_PRODUCT_LIST+"_1_2.jpg";
            this.manuStr+="\n<li><div class=\"txt-on\"  onclick='"+
                          "javascript:$(\"pdt-img\").src = \"/pdt/"+pdt.M_PRODUCT_LIST+"_1_2.jpg\";" +
                          "$(\"ph-pic-img-txt\").innerHTML=\""+pdt.xmlns+"<br/>"+pdt.value+"\";" +
                          "dist.showContent1(\""+pdt.xmlns+"\"));" +
                          "this.style.backgroundColor=\"#8db6d9\"; this.style.color=\"white\";'"+
                          "style='background:#8db6d9'>"+pdt.xmlns+"</div></li>\n";
            var itemColor=pdt.color;
            var colorArr=new Array();
            var ptotCan=0;
            var ptotRem=0;
            colorArr=this.forMetrixChangeToArr(itemColor);
            var sizeArr=colorArr[0].stores[0].docnos[0].tag.size;
            var tagLen=sizeArr.length;
            var item="";
            for(var p=0;p<colorArr.length;p++){
                for(var pp=0;pp<colorArr[p].stores.length;pp++){
                    for(var ppp=0;ppp<colorArr[p].stores[pp].docnos.length;ppp++){
                        for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                            totCan+=colorArr[p].stores[pp].docnos[ppp].tag.can[w]!='non'?parseInt(colorArr[p].stores[pp].docnos[ppp].tag.can[w]):0;
                            totRem+=colorArr[p].stores[pp].docnos[ppp].tag.rem[w]!='non'?parseInt(colorArr[p].stores[pp].docnos[ppp].tag.rem[w]):0;
                            ptotCan+=colorArr[p].stores[pp].docnos[ppp].tag.can[w]!='non'?parseInt(colorArr[p].stores[pp].docnos[ppp].tag.can[w]):0;
                            ptotRem+=colorArr[p].stores[pp].docnos[ppp].tag.rem[w]!='non'?parseInt(colorArr[p].stores[pp].docnos[ppp].tag.rem[w]):0;
                        }
                        for(var con=0;con<4;con++){
                            item+="<tr>";
                            if(pp==0&&ppp==0&&con==0){
                                item+= "<td rowspan=\""+this.forColorSpan(colorArr[p])+"\" valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-left-txt\">"+colorArr[p].name+"</td>";
                            }
                            if(ppp==0&&con==0){
                                item+="<td rowspan=\""+this.forStorSpan(colorArr[p].stores[pp])+"\" valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-left-txt01\">"+colorArr[p].stores[pp].name+"</td>"
                            }
                            if(con==0){
                                item+="<td rowspan=\"4\" valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-bg\">"+colorArr[p].stores[pp].docnos[ppp].no+"</td>"+
                                              "<td rowspan=\"4\" valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-bg\">"+this.forChangeDate(colorArr[p].stores[pp].docnos[ppp].date)+"</td>"+
                                              "<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txt\">配货</td>";
                                for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                    var itemMetrixTr=colorArr[p].stores[pp].docnos[ppp].tag.can[w];
                                    var barCode=colorArr[p].stores[pp].docnos[ppp].tag.barCode[w];
                                    var qtyAl=parseInt(colorArr[p].stores[pp].docnos[ppp].tag.qtyAl[w]);
                                    var docno=colorArr[p].stores[pp].docnos[ppp].no;
                                    qtyAl=isNaN(qtyAl)?0:qtyAl;
                                    item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-bg\""+(itemMetrixTr=='non'?" style=\"background-color:#eeeeee\"":"")+">"+(itemMetrixTr!='non'?"<input title='"+barCode+"' name=\""+docno+"\" type=\"text\" class=\"td-txt-input\" value=\""+(qtyAl==0?'':qtyAl)+"\"/>":"")+"</td>";
                                }
                            }
                            if(con==1){
                                item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txt\">可配</td>";
                                for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                    var itemMetrixTr=colorArr[p].stores[pp].docnos[ppp].tag.can[w];
                                    item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txtK\""+(itemMetrixTr=='non'?" style=\"background-color:#eeeeee\"":"")+">"+(itemMetrixTr!='non'?itemMetrixTr:"")+"</td>";
                                }
                            }
                            if(con==2){
                                item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txt\">未配</td>";
                                for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                    var itemMetrixTr=colorArr[p].stores[pp].docnos[ppp].tag.rem[w];
                                    item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txtW\""+(itemMetrixTr=='non'?" style=\"background-color:#eeeeee\"":"")+">"+(itemMetrixTr!='non'?itemMetrixTr:"")+"</td>";
                                }
                            }
                            if(con==3){
                                item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txt\">订单量</td>";
                                for(var w=0;w<colorArr[p].stores[pp].docnos[ppp].tag.size.length;w++){
                                    var itemMetrixTr=colorArr[p].stores[pp].docnos[ppp].tag.dest[w];
                                    item+="<td valign=\"top\" bgcolor=\"#8db6d9\" class=\"td-right-txtD\""+(itemMetrixTr=='non'?" style=\"background-color:#eeeeee\"":"")+">"+(itemMetrixTr!='non'?itemMetrixTr:"")+"</td>";
                                }
                            }
                            item+="</tr>";
                        }
                    }
                }
            }
            this.itemStr+="<table id='"+pdt.xmlns+ "'  title=\""+ptotCan+":"+ptotRem+"\" border=\"0\" cellpadding=\"0\" cellspacing=\"1\" width=\"\" bgcolor=\"#8db6d9\" style=\"table-layout:fixed;\">\n";
            this.itemStr+=this.forTableShowStyle(tagLen)+"<tr>"+
                          "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">颜色</td>"+
                          "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">店仓</td>" +
                          "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">订单号</td>" +
                          "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">发货日期</td>"+
                          "<td bgcolor=\"#FFFFFF\" class=\"td-left-title\">尺寸</td>";
            for(var e=0;e<sizeArr.length;e++){
                this.itemStr+="<td bgcolor=\"#B6D0E7\" class=\"td-right-title\">"+sizeArr[e]+"</td>";
            };
            this.itemStr+="</tr>";
            this.itemStr+=item;
            this.product[0]={};
            this.product[0].name=pdt.xmlns;
            this.product[0].value=pdt.value;
            this.product[0].colors=colorArr;
            this.itemStr+="</table>";
            $("input-5").innerHTML=ptotCan;
            $("input-4").innerHTML=ptotRem;
        }
        $("category_manu").innerHTML=this.manuStr;
        $("ph-from-right-table").innerHTML=this.itemStr;
        $("tot-can").innerHTML=totCan;
        $("tot-rem").innerHTML=totRem;
        $("fund_balance").value=ret.m_allot_id;
        $("showStyle").value="metrix";
        if($("load_type").value=="reload"){
            $('column_26991').disabled="true";
            jQuery("#Details table td input").attr("disabled","true");
            jQuery("#Details table td img").css("display","none");
            $("ph-serach-img").style.display = "none";
        }
        if($("orderStatus").value=="2"){
            jQuery("#ph-from-right-table td input").attr("disabled","true");
        }
        if(!window.document.addEventListener){
            window.document.attachEvent("onkeydown",hand11);
            function hand11()
            {
                if(window.event.keyCode==13){
                    return false;
                }
            }
        }
        this.autoView1();
    },
    /*
     ×Json对象转化为数组
     ×itemColor:后台传入的color数组或者对象
     */
    forMetrixChangeToArr:function(itemColor){
        var colorArr=new Array();
        if(this.checkIsArray(itemColor)){
            for(var i=0;i<itemColor.length;i++) {
                var itemStor=itemColor[i].c_store;
                var storArr=new Array();
                if(this.checkIsArray(itemStor)){
                    for(var j=0;j<itemStor.length;j++) {
                        var itemDocno=itemStor[j].w.docno;
                        var docnoArr=new Array();
                        if(this.checkIsArray(itemDocno)){
                            for(var jj=0;jj<itemDocno.length;jj++){
                                docnoArr[jj]={};
                                docnoArr[jj].date=itemDocno[jj].billdate;
                                docnoArr[jj].no = itemDocno[jj].xmlns;
                                var tag={};
                                var itemTag=itemDocno[jj].q.array.tag_c;
                                var size=new Array();
                                var can=new Array();
                                var rem=new Array();
                                var dest=new Array();
                                var barCode=new Array();
                                var qtyAl=new Array();
                                if(this.checkIsArray(itemTag)){
                                    for(var s=0;s<itemTag.length;s++) {
                                        size[s]=itemTag[s].content?itemTag[s].content:itemTag[s];
                                        can[s]=itemTag[s].content?itemTag[s].QTYCAN:'non';
                                        rem[s]=itemTag[s].content?itemTag[s].QTYREM:'non';
                                        dest[s]=itemTag[s].content?itemTag[s].DESTQTY:'non';
                                        barCode[s]=itemTag[s].content?itemTag[s].m_product_alias_id:'non';
                                        qtyAl[s]=itemTag[s].content?itemTag[s].QTY_ALLOT:'non';
                                    }
                                }else{
                                    size[0]=itemTag.content?itemTag.content:itemTag;
                                    can[0]=itemTag.content?itemTag.QTYCAN:'non';
                                    rem[0]=itemTag.content?itemTag.QTYREM:'non';
                                    dest[0]=itemTag.content?itemTag.DESTQTY:'non';
                                    barCode[0]=itemTag.content?itemTag.m_product_alias_id:'non';
                                    qtyAl[0]=itemTag.content?itemTag.QTY_ALLOT:'non';
                                }
                                tag.size=size;
                                tag.can=can;
                                tag.rem=rem;
                                tag.dest=dest;
                                tag.barCode=barCode;
                                tag.qtyAl=qtyAl;
                                docnoArr[jj].tag=tag;
                            }
                        }else{
                            docnoArr[0]={};
                            docnoArr[0].date=itemDocno.billdate;
                            docnoArr[0].no = itemDocno.xmlns;
                            var itemTag=itemDocno.q.array.tag_c;
                            var tag={};
                            var size=new Array();
                            var can=new Array();
                            var rem=new Array();
                            var dest=new Array();
                            var barCode=new Array();
                            var qtyAl=new Array();
                            if(this.checkIsArray(itemTag)){
                                for(var s=0;s<itemTag.length;s++) {
                                    size[s]=itemTag[s].content?itemTag[s].content:itemTag[s];
                                    can[s]=itemTag[s].content?itemTag[s].QTYCAN:'non';
                                    rem[s]=itemTag[s].content?itemTag[s].QTYREM:'non';
                                    dest[s]=itemTag[s].content?itemTag[s].DESTQTY:'non';
                                    barCode[s]=itemTag[s].content?itemTag[s].m_product_alias_id:'non';
                                    qtyAl[s]=itemTag[s].content?itemTag[s].QTY_ALLOT:'non';
                                }
                            }else{
                                size[0]=itemTag.content?itemTag.content:itemTag;
                                can[0]=itemTag.content?itemTag.QTYCAN:'non';
                                rem[0]=itemTag.content?itemTag.QTYREM:'non';
                                dest[0]=itemTag.content?itemTag.DESTQTY:'non';
                                barCode[0]=itemTag.content?itemTag.m_product_alias_id:'non';
                                qtyAl[0]=itemTag.content?itemTag.QTY_ALLOT:'non';
                            }
                            tag.size=size;
                            tag.can=can;
                            tag.rem=rem;
                            tag.dest=dest;
                            tag.barCode=barCode;
                            tag.qtyAl=qtyAl;
                            docnoArr[0].tag=tag;
                        }
                        storArr[j]={};
                        storArr[j].name=itemStor[j].xmlns;
                        storArr[j].docnos=docnoArr;
                    }
                }else{
                    var itemDocno=itemStor.w.docno;
                    var docnoArr=new Array();
                    if(this.checkIsArray(itemDocno)){
                        for(var jj=0;jj<itemDocno.length;jj++){
                            docnoArr[jj]={};
                            docnoArr[jj].date=itemDocno[jj].billdate;
                            docnoArr[jj].no = itemDocno[jj].xmlns;
                            var itemTag=itemDocno[jj].q.array.tag_c;
                            var tag={};
                            var size=new Array();
                            var can=new Array();
                            var rem=new Array();
                            var dest=new Array();
                            var barCode=new Array();
                            var qtyAl=new Array();
                            if(this.checkIsArray(itemTag)){
                                for(var s=0;s<itemTag.length;s++) {
                                    size[s]=itemTag[s].content?itemTag[s].content:itemTag[s];
                                    can[s]=itemTag[s].content?itemTag[s].QTYCAN:'non';
                                    rem[s]=itemTag[s].content?itemTag[s].QTYREM:'non';
                                    dest[s]=itemTag[s].content?itemTag[s].DESTQTY:'non';
                                    barCode[s]=itemTag[s].content?itemTag[s].m_product_alias_id:'non';
                                    qtyAl[s]=itemTag[s].content?itemTag[s].QTY_ALLOT:'non';
                                }
                            }else{
                                size[0]=itemTag.content?itemTag.content:itemTag;
                                can[0]=itemTag.content?itemTag.QTYCAN:'non';
                                rem[0]=itemTag.content?itemTag.QTYREM:'non';
                                dest[0]=itemTag.content?itemTag.DESTQTY:'non';
                                barCode[0]=itemTag.content?itemTag.m_product_alias_id:'non';
                                qtyAl[0]=itemTag.content?itemTag.QTY_ALLOT:'non';
                            }
                            tag.size=size;
                            tag.can=can;
                            tag.rem=rem;
                            tag.dest=dest;
                            tag.barCode=barCode;
                            tag.qtyAl=qtyAl;
                            docnoArr[jj].tag=tag;
                        }
                    }else{
                        docnoArr[0]={};
                        docnoArr[0].date=itemDocno.billdate;
                        docnoArr[0].no = itemDocno.xmlns;
                        var itemTag=itemDocno.q.array.tag_c;
                        var tag={};
                        var size=new Array();
                        var can=new Array();
                        var rem=new Array();
                        var dest=new Array();
                        var barCode=new Array();
                        var qtyAl=new Array();
                        if(this.checkIsArray(itemTag)){
                            for(var s=0;s<itemTag.length;s++) {
                                size[s]=itemTag[s].content?itemTag[s].content:itemTag[s];
                                can[s]=itemTag[s].content?itemTag[s].QTYCAN:'non';
                                rem[s]=itemTag[s].content?itemTag[s].QTYREM:'non';
                                dest[s]=itemTag[s].content?itemTag[s].DESTQTY:'non';
                                barCode[s]=itemTag[s].content?itemTag[s].m_product_alias_id:'non';
                                qtyAl[s]=itemTag[s].content?itemTag[s].QTY_ALLOT:'non';
                            }
                        }else{
                            size[0]=itemTag.content?itemTag.content:itemTag;
                            can[0]=itemTag.content?itemTag.QTYCAN:'non';
                            rem[0]=itemTag.content?itemTag.QTYREM:'non';
                            dest[0]=itemTag.content?itemTag.DESTQTY:'non';
                            barCode[0]=itemTag.content?itemTag.m_product_alias_id:'non';
                            qtyAl[0]=itemTag.content?itemTag.QTY_ALLOT:'non';
                        }
                        tag.size=size;
                        tag.can=can;
                        tag.rem=rem;
                        tag.dest=dest;
                        tag.barCode=barCode;
                        tag.qtyAl=qtyAl;
                        docnoArr[0].tag=tag;
                    }
                    storArr[0]={};
                    storArr[0].name=itemStor.xmlns;
                    storArr[0].docnos=docnoArr;
                }
                colorArr[i]={};
                colorArr[i].name=itemColor[i].xmlns;
                colorArr[i].stores=storArr;
            }
        }else{
            var itemStor=itemColor.c_store;
            var storArr=new Array();
            if(this.checkIsArray(itemStor)){
                for(var j=0;j<itemStor.length;j++) {
                    var docnoArr=new Array();
                    var itemDocno=itemStor[j].w.docno;
                    if(this.checkIsArray(itemDocno)){
                        for(var jj=0;jj<itemDocno.length;jj++){
                            docnoArr[jj]={};
                            docnoArr[jj].date=itemDocno[jj].billdate;
                            docnoArr[jj].no = itemDocno[jj].xmlns;
                            var itemTag=itemDocno[jj].q.array.tag_c;
                            var tag={};
                            var size=new Array();
                            var can=new Array();
                            var rem=new Array();
                            var dest=new Array();
                            var barCode=new Array();
                            var qtyAl=new Array();
                            if(this.checkIsArray(itemTag)){
                                for(var s=0;s<itemTag.length;s++) {
                                    size[s]=itemTag[s].content?itemTag[s].content:itemTag[s];
                                    can[s]=itemTag[s].content?itemTag[s].QTYCAN:'non';
                                    rem[s]=itemTag[s].content?itemTag[s].QTYREM:'non';
                                    dest[s]=itemTag[s].content?itemTag[s].DESTQTY:'non';
                                    barCode[s]=itemTag[s].content?itemTag[s].m_product_alias_id:'non';
                                    qtyAl[s]=itemTag[s].content?itemTag[s].QTY_ALLOT:'non';
                                }
                            }else{
                                size[0]=itemTag.content?itemTag.content:itemTag;
                                can[0]=itemTag.content?itemTag.QTYCAN:'non';
                                rem[0]=itemTag.content?itemTag.QTYREM:'non';
                                dest[0]=itemTag.content?itemTag.DESTQTY:'non';
                                barCode[0]=itemTag.content?itemTag.m_product_alias_id:'non';
                                qtyAl[0]=itemTag.content?itemTag.QTY_ALLOT:'non';
                            }
                            tag.size=size;
                            tag.can=can;
                            tag.rem=rem;
                            tag.dest=dest;
                            tag.barCode=barCode;
                            tag.qtyAl=qtyAl;
                            docnoArr[jj].tag=tag;
                        }
                    }else{
                        docnoArr[0]={};
                        docnoArr[0].date=itemDocno.billdate;
                        docnoArr[0].no = itemDocno.xmlns;
                        var itemTag=itemDocno.q.array.tag_c;
                        var tag={};
                        var size=new Array();
                        var can=new Array();
                        var rem=new Array();
                        var dest=new Array();
                        var barCode=new Array();
                        var qtyAl=new Array();
                        if(this.checkIsArray(itemTag)){
                            for(var s=0;s<itemTag.length;s++) {
                                size[s]=itemTag[s].content?itemTag[s].content:itemTag[s];
                                can[s]=itemTag[s].content?itemTag[s].QTYCAN:'non';
                                rem[s]=itemTag[s].content?itemTag[s].QTYREM:'non';
                                dest[s]=itemTag[s].content?itemTag[s].DESTQTY:'non';
                                barCode[s]=itemTag[s].content?itemTag[s].m_product_alias_id:'non';
                                qtyAl[s]=itemTag[s].content?itemTag[s].QTY_ALLOT:'non';
                            }
                        }else{
                            size[0]=itemTag.content?itemTag.content:itemTag;
                            can[0]=itemTag.content?itemTag.QTYCAN:'non';
                            rem[0]=itemTag.content?itemTag.QTYREM:'non';
                            dest[0]=itemTag.content?itemTag.DESTQTY:'non';
                            barCode[0]=itemTag.content?itemTag.m_product_alias_id:'non';
                            qtyAl[0]=itemTag.content?itemTag.QTY_ALLOT:'non';
                        }
                        tag.size=size;
                        tag.can=can;
                        tag.rem=rem;
                        tag.dest=dest;
                        tag.barCode=barCode;
                        tag.qtyAl=qtyAl;
                        docnoArr[0].tag=tag;
                    }
                    storArr[j]={};
                    storArr[j].name=itemStor[j].xmlns;
                    storArr[j].docnos=docnoArr;
                }
            }else{
                var itemDocno=itemStor.w.docno;
                var docnoArr=new Array();
                if(this.checkIsArray(itemDocno)){
                    for(var jj=0;jj<itemDocno.length;jj++){
                        docnoArr[jj]={};
                        docnoArr[jj].date=itemDocno[jj].billdate;
                        docnoArr[jj].no = itemDocno[jj].xmlns;
                        var itemTag=itemDocno[jj].q.array.tag_c;
                        var tag={};
                        var size=new Array();
                        var can=new Array();
                        var rem=new Array();
                        var dest=new Array();
                        var barCode=new Array();
                        var qtyAl=new Array();
                        if(this.checkIsArray(itemTag)){
                            for(var s=0;s<itemTag.length;s++) {
                                size[s]=itemTag[s].content?itemTag[s].content:itemTag[s];
                                can[s]=itemTag[s].content?itemTag[s].QTYCAN:'non';
                                rem[s]=itemTag[s].content?itemTag[s].QTYREM:'non';
                                dest[s]=itemTag[s].content?itemTag[s].DESTQTY:'non';
                                barCode[s]=itemTag[s].content?itemTag[s].m_product_alias_id:'non';
                                qtyAl[s]=itemTag[s].content?itemTag[s].QTY_ALLOT:'non';
                            }
                        }else{
                            size[0]=itemTag.content?itemTag.content:itemTag;
                            can[0]=itemTag.content?itemTag.QTYCAN:'non';
                            rem[0]=itemTag.content?itemTag.QTYREM:'non';
                            dest[0]=itemTag.content?itemTag.DESTQTY:'non';
                            barCode[0]=itemTag.content?itemTag.m_product_alias_id:'non';
                            qtyAl[0]=itemTag.content?itemTag.QTY_ALLOT:'non';
                        }
                        tag.size=size;
                        tag.can=can;
                        tag.rem=rem;
                        tag.dest=dest;
                        tag.barCode=barCode;
                        tag.qtyAl=qtyAl;
                        docnoArr[jj].tag=tag;
                    }
                }else{
                    docnoArr[0]={};
                    docnoArr[0].date=itemDocno.billdate;
                    docnoArr[0].no = itemDocno.xmlns;
                    var itemTag=itemDocno.q.array.tag_c;
                    var tag={};
                    var size=new Array();
                    var can=new Array();
                    var rem=new Array();
                    var dest=new Array();
                    var barCode=new Array();
                    var qtyAl=new Array();
                    if(this.checkIsArray(itemTag)){
                        for(var s=0;s<itemTag.length;s++) {
                            size[s]=itemTag[s].content?itemTag[s].content:itemTag[s];
                            can[s]=itemTag[s].content?itemTag[s].QTYCAN:'non';
                            rem[s]=itemTag[s].content?itemTag[s].QTYREM:'non';
                            dest[s]=itemTag[s].content?itemTag[s].DESTQTY:'non';
                            barCode[s]=itemTag[s].content?itemTag[s].m_product_alias_id:'non';
                            qtyAl[s]=itemTag[s].content?itemTag[s].QTY_ALLOT:'non';
                        }
                    }else{
                        size[0]=itemTag.content?itemTag.content:itemTag;
                        can[0]=itemTag.content?itemTag.QTYCAN:'non';
                        rem[0]=itemTag.content?itemTag.QTYREM:'non';
                        dest[0]=itemTag.content?itemTag.DESTQTY:'non';
                        barCode[0]=itemTag.content?itemTag.m_product_alias_id:'non';
                        qtyAl[0]=itemTag.content?itemTag.QTY_ALLOT:'non';
                    }
                    tag.size=size;
                    tag.can=can;
                    tag.rem=rem;
                    tag.dest=dest;
                    tag.barCode=barCode;
                    tag.qtyAl=qtyAl;
                    docnoArr[0].tag=tag;
                }
                storArr[0]={};
                storArr[0].name=itemStor.xmlns;
                storArr[0].docnos=docnoArr;
            }
            colorArr[0]={};
            colorArr[0].name=itemColor.xmlns;
            colorArr[0].stores=storArr;
        }
        return colorArr;
    },
    forChangeDate:function(date){
      return date.substring(2,4)+"-"+date.substring(4,6)+"-"+date.substring(6);
    },
    forColorSpan:function(color){
        var count=0;
        for(var i=0;i<color.stores.length;i++){
            count+=color.stores[i].docnos.length;
        }
        return count*4;
    },
    forStorSpan:function(stor){
       return stor.docnos.length*4;
    },
    forTableShowStyle:function(tagTot){
        var str= "";
        str+="<col width=\"50\"/><col width=\"70\"/><col width=\"110\"/><col width=\"65\"/><col width=\"65\">";
        for(var i=0;i<tagTot;i++) {
            str+="<col width=\"65\"/>";
        }
        return str;
    },
    _executeCommandEvent :function (evt) {
        Controller.handle( Object.toJSON(evt), function(r){
            //try{

            var result= r.evalJSON();
            if (result.code !=0 ){
                alert(result.message);
            }else {
                var evt=new BiEvent(result.callbackEvent);
                evt.setUserData(result.data);
                application.dispatchEvent(evt);
            }
        });
    },
    checkIsObject:function(o) {
        return (typeof(o)=="object");
    },

    checkIsArray: function(o) {
        return (this.checkIsObject(o) && (o.length) &&(!this.checkIsString(o)));
    },
    checkIsString:function (o) {
        return (typeof(o)=="string");
    },
    checkIsDate:function(month,date,year){
        if(parseInt(month,10)>12||parseInt(month,10)<1||parseInt(date,10)>31||parseInt(date,10)<1||parseInt(year,10)<1980||parseInt(year,10)>3000) {
            return false;
        }
        return true;
    },
    tabToArr:function(tab){
        var cellArr=new Array();
        for(var i=0;i<tab.rows.length;i++){
            cellArr[i]=new Array();
            for(var j=0;j<tab.rows[i].cells.length;j++){
                cellArr[i][j]=tab.rows[i].cells[j];
            }
        }
        return cellArr;
    },
    autoView1:function(){
        window.onbeforeunload=function(){
            if( $("isChanged").value=='true'){
                return "页面数据已改动，还未保存！";
            }else{
                return;
            }
        }
        jQuery("#ph-from-right-table table input").bind("focus",function(event){
            var e=Event.element(event)
            var tab=jQuery(e).parents("table")[0];
            var p=jQuery(e).parents("tr")[0];
            var index=jQuery(p).children().index(jQuery(e).parents()[0]);//*index函数中参数是Element而不是jQuery对象！！！！
            var indexD=tab.rows[0].cells.length-jQuery(p).children().length;
            var rowIndex=jQuery(tab.rows).index(p);
            var col=new Array();
            for(var j=1;j<tab.rows.length;j++){
                var lenC0=tab.rows[0].cells.length;
                var lenCj=tab.rows[j].cells.length;
                col[j-1]=tab.rows[j].cells[index+indexD-(lenC0-lenCj)];
            }
            var allInput=jQuery("#ph-from-right-table table input");
            var totAlready=0;
            for(var s=0;s<allInput.length;s++){
                var vs=isNaN(parseInt(allInput[s].value,10))?0:parseInt(allInput[s].value,10);
                totAlready+=vs;
            }
            var can=0;
            var qty=0;
            for(var i=0;i<col.length;i++) {
                if(col[i]==jQuery(this).parent("td")[0]){
                        can=isNaN(parseInt(col[i+1].innerHTML,10))?0:parseInt(col[i+1].innerHTML,10);
                }
                if(i%4==0) {
                    if(col[i].firstChild&&this.title==col[i].firstChild.title){
                        var coun=col[i].firstChild?col[i].firstChild.value:0;
                        qty+=isNaN(parseInt(coun,10))?0:parseInt(coun,10);
                    }
                }
            }
            var pallInput=jQuery("#"+tab.id+" input");
            var ptotAlready=0;
             for(var ss=0;ss<pallInput.length;ss++){
                var vss=isNaN(parseInt(pallInput[ss].value,10))?0:parseInt(pallInput[ss].value,10);
                ptotAlready+=vss;
            }
            $("input-2").innerHTML=ptotAlready;
            $("tot-ready").innerHTML=totAlready;
            $("input-1").innerHTML=can-qty;
            $("rs").innerHTML=isNaN(parseInt(col[rowIndex+1].innerHTML,10))?0:parseInt(col[rowIndex+1].innerHTML,10);
        });
        jQuery("#ph-from-right-table table input").bind("keyup",function(event){
            var isSel=0;
            jQuery(this).bind("blur",function(){
               isSel++; 
            });
            if(event.target==this){
                this.status=1;
                var e=jQuery(this)[0];
                var tab=jQuery(e).parents("table")[0];
                var p=jQuery(e).parents("tr")[0];
                var rowInput=jQuery(p).find("input");
                var inputIndex=rowInput.index(e);
                var index=jQuery(p).children().index(jQuery(this).parent()[0]);
                var indexD=tab.rows[0].cells.length-jQuery(p).children().length;
                var rowIndex=jQuery(tab.rows).index(p);
                var col=new Array();//所在input列td组
                for(var j=1;j<tab.rows.length;j++){
                    var lenC0=tab.rows[0].cells.length;
                    var lenCj=tab.rows[j].cells.length;
                    col[j-1]=tab.rows[j].cells[index+indexD-(lenC0-lenCj)];
                }

                var colForinput=jQuery(col).find("input");
                var colIndex=colForinput.index(e);
                /*
                 *当输入的是数字的时候响应
                 *动态显示条码当前可配，款号当前已配，总已配*/
                if((event.which>=48&&event.which<=57)||(event.which>=96&&event.which<=105)){
                    var can=0;
                    var qty=0;
                    $("isChanged").value='true';
                    for(var i=0;i<col.length;i++) {
                        if(col[i]==jQuery(this).parent("td")[0]){
                            can=isNaN(parseInt(col[i+1].innerHTML,10))?0:parseInt(col[i+1].innerHTML,10);
                        }
                        if(i%4==0) {
                            if(col[i].firstChild&&col[i].firstChild.title==this.title){
                                var coun=col[i].firstChild?col[i].firstChild.value:0;
                                qty+=isNaN(parseInt(coun,10))?0:parseInt(coun,10);
                            }
                        }
                    }
                    var allInput=jQuery("#ph-from-right-table table input");
                    var totAlready=0;
                    for(var s=0;s<allInput.length;s++){
                        var vs=isNaN(parseInt(allInput[s].value,10))?0:parseInt(allInput[s].value,10);
                        totAlready+=vs;
                    }
                    var pallInput=jQuery("#"+tab.id+" input");
                    var ptotAlready=0;
                    for(var ss=0;ss<pallInput.length;ss++){
                        var vss=isNaN(parseInt(pallInput[ss].value,10))?0:parseInt(pallInput[ss].value,10);
                        ptotAlready+=vss;
                    }
                    $("input-2").innerHTML=ptotAlready;
                    $("input-1").innerHTML=can-qty;
                    $("tot-ready").innerHTML=totAlready;
                    if((can-qty)<0) {
                        alert("当前可配量已小于0！请重新分配！");
                        e.value = 0;
                        dwr.util.selectRange(e,0,100);
                    }
                    var v=isNaN(parseInt(e.value,10))?0:parseInt(e.value,10);
                    if(v>(isNaN(parseInt(col[rowIndex+1].innerHTML,10))?0:parseInt(col[rowIndex+1].innerHTML,10))){
                        alert("配置量大于未配量，请重新配置！");
                        e.value=0;
                        dwr.util.selectRange(e,0,100);
                    }
                }else if(event.which==37){ //响应上下左右键事件，及表格中输入框中移动
                    if(rowInput[inputIndex-1]){
                        rowInput[inputIndex-1].focus();
                    }else{
                        rowInput[rowInput.length-1].focus();
                    }
                }else if(event.which==39){
                    if(rowInput[inputIndex+1]){
                        rowInput[inputIndex+1].focus();
                    }else{
                        rowInput[0].focus();
                    }
                }else if(event.which==38){
                    if(colForinput[colIndex-1]){
                        colForinput[colIndex-1].focus();
                    }else{
                        colForinput[colForinput.length-1].focus();
                    }
                }else if(event.which==40){
                    if(colForinput[colIndex+1]){
                        colForinput[colIndex+1].focus();
                    }else{
                        colForinput[0].focus();
                    }
                }else if(event.which==13){
                    if(isSel==0){
                        if(jQuery("#ph-from-right-table table input")[jQuery("#ph-from-right-table table input").index(this)+1]){
                            jQuery("#ph-from-right-table table input")[jQuery("#ph-from-right-table table input").index(this)+1].focus();
                        }else{
                            jQuery("#ph-from-right-table table input")[0].focus();
                        }
                    }
                }
            }
        });
    },
    showContent:function(e){
        var lies=$("category_manu").getElementsByTagName("li");
        var tabs=$("ph-from-right-table").getElementsByTagName("table");
        for(var d=0;d<lies.length;d++){
            lies[d].firstChild.style.backgroundColor="";
            lies[d].firstChild.style.color="";
        }
        for(var i=0;i<tabs.length;i++){
            tabs[i].style.display="none";
        }
        if(e){
            e.style.display="";
            this.pdt_data(e);
        }
    },
    showContent1:function(tb,type){
        var lies=$("category_manu").getElementsByTagName("li");
        var tabs=$("ph-from-right-table").getElementsByTagName("table");
		if(type!="no"){
			for(var d=0;d<lies.length;d++){
				lies[d].firstChild.style.backgroundColor="";
				lies[d].firstChild.style.color="";
			}
		}
        for(var i=0;i<tabs.length;i++){
            tabs[i].style.display="none";
        }
        $(tb).style.display="";
        var strA=$(tb).title.split(":");
        $("input-5").innerHTML=strA[0];
        $("input-4").innerHTML=strA[1];
    },
    pdt_data:function(e){
        var arr=this.tabToArr(e);
        var pdtrs=0;
        var pdtrem=0;
        for(var i=0;i<arr.length;i++){
            pdtrs+=parseInt(arr[i][7].firstChild.innerHTML.trim(),10);
            pdtrem+=parseInt(arr[i][6].firstChild.innerHTML.trim(),10);
        }
        $("input-5").innerHTML=pdtrem;
        $("input-4").innerHTML=pdtrs;
        var tab=$("table_title_tot");
        this.equal_w(tab,e);
    },
    equal_w:function(tab1,tab2){
        var cells1=tab1.rows[0];
        var cells2=tab2.rows[0];
        for(var i=0;i<cells1.length;i++){
            cells1[i].width=cells2[i].width||cells2.style.width;
        }
    },
    pdt_search:function(){
        var cdt=$("pdt-search").value;
        if($("showStyle").value=="metrix"){
            var pdts=new Array();
            if(!cdt||!cdt.trim()){
                for(var i=0;i<this.product.length;i++){
					pdts[i]=this.product[i];
				}
				$("category_manu").innerHTML=this.manuStr;
            }else{
				cdt=cdt.strip();
				var manuStr="";
				var reg= new RegExp(cdt,"i");
				 for(var i=0;i<this.product.length;i++){
					if(reg.test(this.product[i].name)){
						pdts.push(this.product[i]);
					}
				}
				for(var j=0;j<pdts.length;j++){
					manuStr+="\n<li><div class=\"txt-on\"  onclick='javascript:$(\"pdt-img\").src = \"/pdt/"+pdts[j].M_PRODUCT_LIST+"_1_2.jpg\";" +
                              "$(\"ph-pic-img-txt\").innerHTML=\""+pdts[j].name+"<br/>"+pdts[j].value+"\";" +
                              "dist.showContent1(\""+pdts[j].name+"\");" +
                              "this.style.backgroundColor=\"#8db6d9\"; this.style.color=\"white\";'"+
                              (j==0?"  style='background:#8db6d9'":"")+">"+pdts[j].name+"</div></li>\n";
				}
				$("category_manu").innerHTML=manuStr;
			}
			this.showContent1(pdts[0].name,"no");
        }else{
            this.showContent();
            if(!cdt||!cdt.trim()){
                if(this.checkIsArray(this.manu)){
                    $("ph-pic-img-txt").innerHTML=this.manu[0].name+" <br/>"+this.manu[0].value;
                    $("pdt-img").src = "/pdt/"+this.manu[0].M_PRODUCT_LIST+"_1_2.jpg";
                    this.showContent($(this.manu[0].M_PRODUCT_LIST));
                }else {
                    $("ph-pic-img-txt").innerHTML=this.manu.name+" <br/>"+this.manu.value;
                    $("pdt-img").src = "/pdt/"+this.manu.M_PRODUCT_LIST+"_1_2.jpg";
                    this.showContent($(this.manu.M_PRODUCT_LIST));
                }
                $("category_manu").innerHTML=this.manuStr;
            }else{
                cdt=cdt.trim();
                var reg=new RegExp(cdt,"i");
                var strTemp="";
                if(this.checkIsArray(this.manu)){
                    var count=0;
                    for(var i=0;i<this.manu.length;i++){
                        if(reg.test(this.manu[i].name)){
                            count++;
                            strTemp+="\n<li><div class=\"txt-on\"  onclick='javascript:$(\"pdt-img\").src = \"/pdt/"+this.manu[i].M_PRODUCT_LIST+"_1_2.jpg\";$(\"ph-pic-img-txt\").innerHTML=\""+this.manu[i].name+"<br/>"+this.manu[i].value+"\";dist.showContent(document.getElementById(\""+this.manu[i].M_PRODUCT_LIST+"\"));this.style.backgroundColor=\"#0099cc\"'"+(count==1?"  style='background:#0099cc'":"")+">"+this.manu[i].name+"</div></li>\n";
                            if(count==1){
                                $("ph-pic-img-txt").innerHTML=this.manu[i].name+" <br/>"+this.manu[i].value;
                                $("pdt-img").src = "/pdt/"+this.manu[i].M_PRODUCT_LIST+"_1_2.jpg";
                                this.showContent($(this.manu[i].M_PRODUCT_LIST));
                            }
                        }
                    }
                }else if(reg.test(this.manu.name)){
                    strTemp="\n<li><div class=\"txt-on\"  onclick='javascript:$(\"pdt-img\").src = \"/pdt/"+this.manu.M_PRODUCT_LIST+"_1_2.jpg\";$(\"ph-pic-img-txt\").innerHTML=\""+this.manu.name+"<br/>"+this.manu.value+"\";dist.showContent(document.getElementById(\""+this.manu.M_PRODUCT_LIST+"\"));this.style.backgroundColor=\"#0099cc\"'  style='background:#0099cc'>"+this.manu.name+"</div></li>\n";
                    $("ph-pic-img-txt").innerHTML=this.manu.name+" <br/>"+this.manu.value;
                    $("pdt-img").src = "/pdt/"+this.manu.M_PRODUCT_LIST+"_1_2.jpg";
                    this.showContent($(this.manu.M_PRODUCT_LIST));
                }
                $("category_manu").innerHTML=strTemp;
            }
            var orig_out=$("column_26993_fd");
            var tabs=$("ph-from-right-table").getElementsByTagName("table");
            var tot_can=0;
            var tot_rem=0;

            for(var i=0;i<tabs.length;i++){
                var inp=$("input-1");
                var inp2=$("rs");
                var inp3=$("input-2");

                var arrCell=this.tabToArr(tabs[i]);
                for(var j=0;j<arrCell.length;j++){
                    tot_can+=parseInt(arrCell[j][7].firstChild.innerHTML,10);
                    tot_rem+=parseInt(arrCell[j][6].firstChild.innerHTML,10);
                }
                this.autoView(8,orig_out,tabs[i],inp,inp2,inp3);
                this.keyListener(tabs[i],8);
            }
            $("tot-can").innerHTML=tot_can;
            $("tot-rem").innerHTML=tot_rem;
        }
    },
    showObject:function(url, theWidth, theHeight,option){
        if( theWidth==undefined || theWidth==null) theWidth=956;
        if( theHeight==undefined|| theHeight==null) theHeight=570;
        var options={width:theWidth,height:theHeight,title:gMessageHolder.IFRAME_TITLE, modal:true,centerMode:"xy",maxButton:true};
        if(option!=undefined)
            Object.extend(options, option);
        Alerts.popupIframe(url,options);
        Alerts.resizeIframe(options);
        Alerts.center();
    }
}
DIST.main = function () {
    dist=new DIST();
};

/**
 * Init
 */
jQuery(document).ready(DIST.main);
jQuery(document).ready(function(){
   jQuery("body").bind("keyup",function(event){
       if(event.which==13){
           event.stopPropagation();
       }
   });
});

String.prototype.trim=function(){
    return this.replace(/(^\s*)|(\s*$)/g, '');
}
/**
 *删除左边的空格
 */
String.prototype.ltrim=function()
{
    return this.replace(/(^s*)/g,'');
}
/**
 *删除右边的空格
 */
String.prototype.rtrim=function()
{
    return this.replace(/(s*$)/g,'');
}