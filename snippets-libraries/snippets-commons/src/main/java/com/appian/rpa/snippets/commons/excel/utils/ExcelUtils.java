package com.appian.rpa.snippets.commons.excel.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;

public final class ExcelUtils {

	/**
	 * Private constructor
	 */
	private ExcelUtils() {

	}

	/**
	 * Indicates if the sheet is hidden.
	 * 
	 * @param workbook   Workbook where the sheet is located
	 * @param sheetIndex Sheet index
	 * @return
	 */
	public static boolean isSheetHidden(Workbook workbook, int sheetIndex) {

		return workbook.isSheetHidden(sheetIndex) || workbook.isSheetVeryHidden(sheetIndex);
	}

	/**
	 * Select and active only the given sheet.
	 * 
	 * @param workbook   Workbook where the sheet is located
	 * @param sheetIndex Sheet index
	 */
	public static void selectAndActivateOnlyGivenSheet(Workbook workbook, int sheetIndex) {

		for (int index = 0; index < workbook.getNumberOfSheets(); index++) {

			if (isSheetHidden(workbook, index)) {
				continue;
			}

			if (index == sheetIndex) {
				workbook.getSheetAt(sheetIndex).setSelected(true);
				workbook.setActiveSheet(sheetIndex);
				continue;
			}

			workbook.getSheetAt(index).setSelected(false);
		}
	}

	/**
	 * Select and active only the given sheet.
	 * 
	 * @param workbook  Workbook where the sheet is located
	 * @param sheetName Sheet name
	 */
	public static void selectAndActivateOnlyGivenSheet(Workbook workbook, String sheetName) {

		int sheetIndex;

		if (StringUtils.isBlank(sheetName)) {

			sheetIndex = workbook.getActiveSheetIndex();

		} else {

			int i = workbook.getSheetIndex(sheetName);
			sheetIndex = i == -1 ? workbook.getActiveSheetIndex() : i;
		}

		selectAndActivateOnlyGivenSheet(workbook, sheetIndex);
	}

}
