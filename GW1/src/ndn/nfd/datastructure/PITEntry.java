package ndn.nfd.datastructure;

import java.util.ArrayList;
import java.util.List;

//import ndn.nfd.face.Face;
import net.named_data.jndn.Face;

/**
 * PIT のエントリ
 * @author taku
 *
 */
public class PITEntry extends TableEntry {

	protected List<Face> inRecord;
	protected List<Face> outRecord;
	
	public PITEntry(String name) {
		super(name);
		this.inRecord = new ArrayList<Face>();
		this.outRecord = new ArrayList<Face>();
	}
	
	public List<Face> getInRecord() {
		return inRecord;
	}
	
	public void setInRecord(List<Face> inRecord) {
		this.inRecord = inRecord;
	}
	
	public void addInFace(Face inFace) {
		if (inFace == null)
			return;
		if (!isInFace(inFace))
			this.inRecord.add(inFace);
	}	
	
	public boolean isInFace(Face inFace) {
		for (int i=0; i<inRecord.size(); i++) {
			if (inFace.getFaceID() == inRecord.get(i).getFaceID())
				return true;
		}
		return false;
	}
	
	public List<Face> getOutRecord() {
		return outRecord;
	}
	
	public void setOutRecord(List<Face> outRecord) {
		this.outRecord = outRecord;
	}
	
	public void addOutFace(Face outFace) {
		this.outRecord.add(outFace);
	}
	
	public void removeinRecord(Face inFace) {
		this.inRecord.remove(inFace);
	}
	
	public void removeOutRecord(Face outFace) {
		this.outRecord.remove(outFace);
	}
	
}
