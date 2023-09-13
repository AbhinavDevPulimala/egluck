/**
 *  Business Engine Extension
 */
/****************************************************************************************
 Extension Name: UpdDelMethod
 Type : ExtendM3Transaction
 Script Author: Abhinav Dev Pulimala
 Date: 2023-08-23
 Description: Update New delivery method in User-defined field 5 (OAUCA5) of OOHEAD
 Revision History:
 Name                        Date             Version          Description of Changes
 Abhinav Dev Pulimala        2023-08-23       1.0              Update New delivery method in User-defined field 5 (OAUCA5) of OOHEAD
 ******************************************************************************************/

/**
 * EXT001MI/UpdDelMethod is an MI to update the New delivery method to OOHEAD table via an MI Transaction
 * Parameters: (All parameters are not mandatory)
 * CONO - Company
 * ORNO - Order number (Mandatory)
 * UCA5 - User-defined field 5 (Mandatory)
 */
 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpdDelMethod extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final ProgramAPI program;
	String ORNO;
	String UCA5;

	public UpdDelMethod(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
		this.mi = mi;
		this.database = database;
		this.program = program;
	}
	
	public void main() {
	  int CONO = mi.getIn().get("CONO") == null ? (int) program.getLDAZD().get("CONO") : (int) mi.getIn().get("CONO");
		ORNO = (String) mi.getIn().get("ORNO");
		UCA5 = (String) mi.getIn().get("UCA5");
		// Update New delivery method to User-defined field 5
		DBAction queryToWrite = database.table("OOHEAD")
				.index("00")
				.selection("OACONO", "OAORNO")
				.build();
		DBContainer containerToWrite = queryToWrite.getContainer();
		containerToWrite.setInt("OACONO", CONO);
		containerToWrite.set("OAORNO", ORNO);
		queryToWrite.readLock(containerToWrite,updateCallBack);
	}
	
	Closure < ?> updateCallBack = { LockedResult lockedResult ->
		Integer lmdt = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer;
		Integer chno = (Integer) lockedResult.get('OACHNO') + 1
		String chid = this.program.getUser();
		lockedResult.set("OAUCA5", UCA5);
		lockedResult.set('OALMDT', lmdt);
		lockedResult.set('OACHNO', chno);
		lockedResult.set('OACHID', chid);
		lockedResult.update();
	}
}
