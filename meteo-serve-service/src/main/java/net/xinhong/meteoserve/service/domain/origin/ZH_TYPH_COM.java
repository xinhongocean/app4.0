package net.xinhong.meteoserve.service.domain.origin;

import java.util.Date;

/**
 * Created by wingsby on 2017/10/26.
 */
public class ZH_TYPH_COM {
    private String INTNUM;
    private Date ADATE;
    private  int grid;
    private int GRADE;
    private float CLAT;
    private float CLON;
    private float PRESS;
    private float MAXWS;
    private String indentify;


    public String getINTNUM() {
        return INTNUM;
    }

    public void setINTNUM(String INTNUM) {
        this.INTNUM = INTNUM;
    }



    public Date getADATE() {
        return ADATE;
    }

    public void setADATE(Date ADATE) {
        this.ADATE = ADATE;
    }



    public int getGRADE() {
        return GRADE;
    }

    public void setGRADE(int GRADE) {
        this.GRADE = GRADE;
    }

    public float getCLAT() {
        return CLAT;
    }

    public void setCLAT(float CLAT) {
        this.CLAT = CLAT;
    }

    public float getCLON() {
        return CLON;
    }

    public void setCLON(float CLON) {
        this.CLON = CLON;
    }

    public float getPRESS() {
        return PRESS;
    }

    public void setPRESS(float PRESS) {
        this.PRESS = PRESS;
    }

    public float getMAXWS() {
        return MAXWS;
    }

    public void setMAXWS(float MAXWS) {
        this.MAXWS = MAXWS;
    }

    public int getGrid() {
        return grid;
    }

    public void setGrid(int grid) {
        this.grid = grid;
    }

    public String getIndentify() {
        return indentify;
    }

    public void setIndentify(String indentify) {
        this.indentify = indentify;
    }
}
