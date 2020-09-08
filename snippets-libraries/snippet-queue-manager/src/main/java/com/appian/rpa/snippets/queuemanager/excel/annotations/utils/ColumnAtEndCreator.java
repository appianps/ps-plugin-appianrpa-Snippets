package com.appian.rpa.snippets.queuemanager.excel.annotations.utils;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;

import com.novayre.jidoka.data.provider.api.IExcel;

/**
 * This column creator creates the new columns in the next column from the last
 * used column.
 */
public class ColumnAtEndCreator {

	/**
	 * Create the columns with the titles {@code columnsName} in the title row {@code titleRowIndex}.
	 * 
	 * @param excel IExcel instance where to create the columns
	 * @param columnsName Columns to create name
	 * @param titleRowIndex Index where to create the new columns
	 */
	public void createColumns(IExcel excel, List<String> columnsName, int titleRowIndex) {

		List<Cell> cellList = excel.getEntireRow(titleRowIndex);
		int lastColumnUsedIndex = cellList.get(cellList.size() - 1).getColumnIndex();

		for (String title : columnsName) {

			CellReference cellReference = new CellReference(titleRowIndex, ++lastColumnUsedIndex);

			excel.getCellWithCreation(cellReference).setCellValue(title);
		}
	}
}
