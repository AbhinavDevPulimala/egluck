/**
 *  Business Engine Extension
 */
/****************************************************************************************
 Extension Name: UpdToteNumber
 Type : ExtendM3Transaction
 Script Author: Abhinav Dev Pulimala
 Date: 2023-08-25
 Description: Update Tote number in Delivery specification (DLSP) field of MPTRNS
 Revision History:
 Name                        Date             Version          Description of Changes
 Abhinav Dev Pulimala        2023-08-25       1.0              Update Tote number in Delivery specification (DLSP) field of MPTRNS
 ******************************************************************************************/

/**
 * EXT001MI/UpdToteNumber is an MI to update the Tote number to MPTRNS table via an MI Transaction
 * Parameters: (All parameters are mandatory)
 * CONO - Company
 * DIPA - Disconnected package (Mandatory)
 * WHLO - Warehouse (Mandatory)
 * DLIX - Delivery number (Mandatory)
 * PANR - Package number (Mandatory)
 * DLSP - Delivery specification (Mandatory)
 */

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UpdToteNumber extends ExtendM3Transaction {
	private final MIAPI mi;
	private final DatabaseAPI database;
	private final ProgramAPI program;
	String WHLO;
	String PANR;
	String DLSP;

	public UpdToteNumber(MIAPI mi, DatabaseAPI database, ProgramAPI program) {
		this.mi = mi;
		this.database = database;
		this.program = program;
	}

	public void main() {
		int CONO = mi.getIn().get("CONO") == null ? (int) program.getLDAZD().get("CONO") : (int) mi.getIn().get("CONO");
		int DIPA = (int) mi.getIn().get("DIPA");
		long DLIX = (long) mi.getIn().get("DLIX");
		WHLO = (String) mi.getIn().get("WHLO");
		PANR = (String) mi.getIn().get("PANR");
		DLSP = (String) mi.getIn().get("DLSP");
		// Update Tote number in MPTRNS table
		DBAction queryToWrite = database.table("MPTRNS")
				.index("00")
				.selection("ORCONO", "ORDIPA", "ORWHLO", "ORDLIX", "ORPANR")
				.build();
		DBContainer containerToWrite = queryToWrite.getContainer();
		containerToWrite.setInt("ORCONO", CONO);
		containerToWrite.setInt("ORDIPA", DIPA);
		containerToWrite.setLong("ORDLIX", DLIX);
		containerToWrite.set("ORWHLO", WHLO);
		containerToWrite.set("ORPANR", PANR);
		queryToWrite.readLock(containerToWrite,updateCallBack);
	}

	Closure < ? > updateCallBack = { LockedResult lockedResult ->
		Integer lmdt = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) as Integer;
		Integer chno = (Integer) lockedResult.get('ORCHNO') + 1
		String chid = this.program.getUser();
		lockedResult.set("ORDLSP", DLSP);
		lockedResult.set('ORLMDT', lmdt);
		lockedResult.set('ORCHNO', chno);
		lockedResult.set('ORCHID', chid);
		lockedResult.update();
	}
}
