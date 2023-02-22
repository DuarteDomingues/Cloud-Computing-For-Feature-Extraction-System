package cn.operations;

import com.google.cloud.Date;
import com.google.cloud.firestore.GeoPoint;

import java.util.HashMap;

public class Event {
    public HashMap<String, String> localizacao;
    public GeoPoint point;
    public HashMap<String, String> coordenadas;
    public int objID;
    public int eventID;
    public String eventName;
    public String freguesia;
    public String local;
    public java.util.Date start;
    public java.util.Date end;
    public int code;
    public int idType;
    public int numParticipantes;
    public String active;
    public Date licenceDate;
    public String guid;
    // X,Y,OBJECTID,ID_EVENTO,NOME_EVENTO,TIPO_EVENTO,
    // FREGUESIA,LOCAL,DATA_INICIO,DATA_FIM,COD_SIG,
    // IDTIPO,ATIVO,DATA_SAIDA_LICENCA,GlobalID

    public Event() {}
}
