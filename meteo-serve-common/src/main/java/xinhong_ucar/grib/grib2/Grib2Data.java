/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package xinhong_ucar.grib.grib2;


import xinhong_ucar.grib.QuasiRegular;
import xinhong_ucar.unidata.io.RandomAccessFile;

import java.io.IOException;

/**
 * A class used to extract data from a Grib2 file.
 * see <a href="../../../IndexFormat.txt"> IndexFormat.txt</a>
 *
 * @author Robb Kambic
 * modified by stone
 */
public final class Grib2Data {

  private final RandomAccessFile raf;
  private Grib2GridDefinitionSection gds = null;
  private Grib2ProductDefinitionSection pds = null;
  private Grib2DataRepresentationSection drs = null;
    private Grib2BitMapSection bms = null;
//  private Grib2DataSection ds = null;
    private float[] dataAry;

  /**
   * 获取网格定义段 3
   * @return
   */
  public Grib2GridDefinitionSection getGds() {
    return gds;
  }
  public void setGds(Grib2GridDefinitionSection gds) {
    this.gds = gds;
  }
  /**
   * 获取产品定义段4
   * @return
   */
  public Grib2ProductDefinitionSection getPds() {
    return pds;
  }
  
  public void setPds(Grib2ProductDefinitionSection pds) {
    this.pds = pds;
  }
  /**
   * 获取数据表示段5
   * @return
   */
  public Grib2DataRepresentationSection getDrs() {
    return drs;
  }
  
  public void setDrs(Grib2DataRepresentationSection drs) {
    this.drs = drs;
  }
  /**
   * 获取位图段 6
   * @return
   */
  public Grib2BitMapSection getBms() {
    return bms;
  }
  
  public void setBms(Grib2BitMapSection bms) {
    this.bms = bms;
  }
//  /**
//   * 获取数据段 7
//   * @return
//   */
//  public Grib2DataSection getDs() {
//    return ds;
//  }
//  
//  public void setDs(Grib2DataSection ds) {
//    this.ds = ds;
//  }

  /**
   * 获取数据信息
   * 若调用此方法获取数据，需先调用getData（）
   */
  public float[] getDataAry() {
    return dataAry;
  }
  public void setDataAry(float[] dataAry) {
    this.dataAry = dataAry;
  }
/**
   * Constructs a  Grib2Data object for a RandomAccessFile.
   *
   * @param raf xinhong_ucar.unidata.io.RandomAccessFile with GRIB content
   */
  public Grib2Data(RandomAccessFile raf) {
    this.raf = raf;
  }
  
  /**
   * Reads the Grib data with a certain offsets in the file.
   *
   * @param gdsOffset position in record where GDS starts
   * @param pdsOffset position in record where PDS starts
   * @param refTime reference time in msecs  LOOK WTF ??
   * @return float[] the data
   * @throws IOException if raf does not contain a valid GRIB record.
   */
  public final void getData1(long gdsOffset, long pdsOffset, long refTime) throws IOException {
    boolean expandQuasi = true;
    raf.seek(gdsOffset);

    // Need section 3, 4, 5, 6, and 7 to read/interpet the data//网格定义段 ok
    Grib2GridDefinitionSection gds = new Grib2GridDefinitionSection(raf, false);  // Section 3 no checksum
    this.setGds(gds);
    
    Grib2ProductDefinitionSection pds = new Grib2ProductDefinitionSection(raf, refTime);  // Section 4//产品定义段
    this.setPds(pds);
    
    Grib2DataRepresentationSection drs = new Grib2DataRepresentationSection(raf);  // Section 5//数据表示段
    this.setDrs(drs);

    Grib2BitMapSection bms = new Grib2BitMapSection(true, raf, gds);  // Section 6//位图段
    this.setBms(bms);
    if (bms.getBitmapIndicator() == 254) { //previously defined in the same GRIB2 record
      long offset = raf.getFilePointer();
      raf.seek(gdsOffset);                // go get it
      //Grib2GridDefinitionSection savegds = gds;
      gds = new Grib2GridDefinitionSection(raf, false);
      Grib2ProductDefinitionSection savepds = pds;
      pds = new Grib2ProductDefinitionSection(raf, refTime);  // Section 4

      Grib2DataRepresentationSection savedrs = drs;
      drs = new Grib2DataRepresentationSection(raf);  // Section 5

      bms = new Grib2BitMapSection(true, raf, gds);  // Section 6

      // reset pds, drs
      pds = savepds;
      drs = savedrs;
      raf.seek(offset);
    }

    // Get the data
    Grib2DataSection ds = new Grib2DataSection(true, raf, gds, drs, bms);  // Section 7//数据段
    //System.out.println("DS offset=" + ds.getOffset() );

    // not a quasi grid or don't expand Quasi
    if ((gds.getGdsVars().getOlon() == 0) || !expandQuasi) {
      this.setDataAry(ds.getData());
    } else {
      QuasiRegular qr = new QuasiRegular(ds.getData(), (Object) gds);
//      return qr.getData();
      this.setDataAry(qr.getData());
    }
  }  // end getData

  /**
   * Reads the Grib data with a certain offsets in the file.
   *
   * @param gdsOffset position in record where GDS starts
   * @param pdsOffset position in record where PDS starts
   * @param refTime reference time in msecs  LOOK WTF ??
   * @return float[] the data
   * @throws IOException if raf does not contain a valid GRIB record.
   */
  public final float[] getData(long gdsOffset, long pdsOffset, long refTime) throws IOException {
    //long start = System.currentTimeMillis();

    /*
     *  Expand Quasi-Regular grids
    */
    boolean expandQuasi = true;
    raf.seek(gdsOffset);

    // Need section 3, 4, 5, 6, and 7 to read/interpet the data//网格定义段 ok
    Grib2GridDefinitionSection gds = new Grib2GridDefinitionSection(raf, false);  // Section 3 no checksum
    

    //modified by stone
//    raf.seek(pdsOffset);  // could have more than one pds for a gds
    ///////////////////////////
    
    Grib2ProductDefinitionSection pds = new Grib2ProductDefinitionSection(raf, refTime);  // Section 4//产品定义段

    Grib2DataRepresentationSection drs = new Grib2DataRepresentationSection(raf);  // Section 5//数据表示段

    Grib2BitMapSection bms = new Grib2BitMapSection(true, raf, gds);  // Section 6//位图段
    if (bms.getBitmapIndicator() == 254) { //previously defined in the same GRIB2 record
      long offset = raf.getFilePointer();
      raf.seek(gdsOffset);                // go get it
      //Grib2GridDefinitionSection savegds = gds;
      gds = new Grib2GridDefinitionSection(raf, false);
      Grib2ProductDefinitionSection savepds = pds;
      pds = new Grib2ProductDefinitionSection(raf, refTime);  // Section 4

      Grib2DataRepresentationSection savedrs = drs;
      drs = new Grib2DataRepresentationSection(raf);  // Section 5

      bms = new Grib2BitMapSection(true, raf, gds);  // Section 6

      // reset pds, drs
      pds = savepds;
      drs = savedrs;
      raf.seek(offset);
    }

    // Get the data
    Grib2DataSection ds = new Grib2DataSection(true, raf, gds, drs, bms);  // Section 7//数据段
    //System.out.println("DS offset=" + ds.getOffset() );

    // not a quasi grid or don't expand Quasi
    if ((gds.getGdsVars().getOlon() == 0) || !expandQuasi) {
      return ds.getData();
    } else {
      QuasiRegular qr = new QuasiRegular(ds.getData(), (Object) gds);
      return qr.getData();
    }
  }  // end getData
  
  
}  // end Grib2Data


