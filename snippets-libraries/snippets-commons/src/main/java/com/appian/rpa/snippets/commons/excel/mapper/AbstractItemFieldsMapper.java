package com.appian.rpa.snippets.commons.excel.mapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellReference;

import com.appian.rpa.snippets.commons.excel.annotations.AExcelField;
import com.appian.rpa.snippets.commons.excel.annotations.AExcelFieldKey;
import com.appian.rpa.snippets.commons.excel.annotations.AnnotationUtil;
import com.appian.rpa.snippets.commons.excel.annotations.utils.ColumnAtEndCreator;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.data.provider.api.IExcel;
import com.novayre.jidoka.data.provider.api.IRowMapper;

public abstract class AbstractItemFieldsMapper<T> implements IRowMapper<IExcel, T> {

	/** Boolean chechExcelFieldsKey */
	private boolean checkExcelFieldsKey = true;

	/** Default column creator. */
	private ColumnAtEndCreator columnCreator = new ColumnAtEndCreator();

	/** Indicates if the mapper has been initialized. */
	private boolean rowMapperInitialized = false;

	/** First row of the excel file with data */
	private static final int FIRST_ROW = 1;

	/** Excel sheet default name */
	private static final String SHEET_NAME = "Sheet1";

	/** Queue attempts by default number */
	private static final int ATTEMPTS_BY_DEFAULT = 1;

	/**
	 * Map with the column index for every pair of titleRegExp and ocurrence. - Key:
	 * Pair with: - Left: TitleRegExp - Right: Ocurrence - Value: Column index
	 */
	private Map<Pair<String, Integer>, Integer> titlePositionMap = new HashMap<>();

	/**
	 * AbstractItemFieldsMapper constructor.
	 */
	public AbstractItemFieldsMapper() {

	}

	/**
	 * Returns the T class.
	 * 
	 * @return T class
	 */
	public abstract Class<T> getTClass();

	/**
	 * Gets the first row to search in the Excel file. If the Excel file has a table
	 * header on the first row, the first row with data is 0. In other case is 1.
	 * 
	 * @return First row to search
	 */
	public int getFirstRow() {
		return FIRST_ROW;
	}

	/**
	 * Gets the Excel file sheet name
	 * 
	 * @return Excel file sheet name
	 */
	public String getSheetName() {
		return SHEET_NAME;
	}

	/**
	 * Gets the queue attempts by default
	 * 
	 * @return Queue attempts by default
	 */
	public int getAttemptsByDefault() {
		return ATTEMPTS_BY_DEFAULT;
	}

	/**
	 * @see excel.AbstractItemFieldsMapper#isLastRow(java.lang.Object)
	 */
	@Override
	public boolean isLastRow(T row) {

		if (row == null) {
			return true;
		}

		String keyValue = AnnotationUtil.getKeyFieldValue(row);

		return StringUtils.isBlank(keyValue);
	}

	/**
	 * @see com.novayre.jidoka.data.provider.api.IRowMapper#map(java.lang.Object,
	 *      int)
	 */
	@Override
	public T map(IExcel data, int rowNum) {

		if (!rowMapperInitialized) {
			initializeMapper(data);
		}

		T row = null;

		List<Field> queueFields = AnnotationUtil.getFieldsWithAnnotation(getTClass(), AExcelField.class);

		for (Field field : queueFields) {

			row = mapField(data, rowNum, row, field);
		}

		return row;
	}

	/**
	 * @see com.novayre.jidoka.data.provider.api.IRowMapper#update(java.lang.Object,
	 *      int, java.lang.Object)
	 */
	@Override
	public void update(IExcel data, int rowNum, T row) {

		List<Field> fields = AnnotationUtil.getFieldsWithAnnotation(getTClass(), AExcelField.class);

		for (Field field : fields) {

			AExcelField annotation = field.getAnnotation(AExcelField.class);

			String columnNameRegExp = getFieldNameRegExp(annotation);

			Integer fieldColumnIndex = getColumnIndex(columnNameRegExp);

			if (fieldColumnIndex == null) {
				throw new JidokaFatalException("The column to be updated could not be found: " + columnNameRegExp);
			}

			CellReference cellReference = new CellReference(rowNum, fieldColumnIndex);

			Object value;
			try {
				value = AnnotationUtil.getFieldValue(row, field);
			} catch (JidokaException e) {
				throw new JidokaFatalException("Error while updating Excel", e);
			}

			if (value == null) {
				continue;
			}

			String valueS = "";

			if (value instanceof Double || value instanceof Integer || value instanceof Long) {

				data.getCellWithCreation(cellReference).setCellValue(Double.valueOf(value.toString()));
			} else {

				valueS = value.toString();
				data.getCellWithCreation(cellReference).setCellValue(valueS);
			}
		}
	}

	/**
	 * Initializes the mapper.
	 * 
	 * @param data {@link IExcel} instance
	 */
	private void initializeMapper(IExcel data) {

		resetHeaders();

		checkFieldsKey();

		initTitlePositionMap();

		List<Pair<String, Integer>> excelColumns = localizeExcelColumns(data, getFirstRow());

		calcTitlePositionMap(excelColumns);

		initCreateColumns(data, getFirstRow());

		rowMapperInitialized = true;

	}

	/**
	 * Resets the {@code AbstractItemFieldsMapper} headers
	 */
	public void resetHeaders() {

		rowMapperInitialized = false;
		titlePositionMap.clear();
	}

	/**
	 * Checks if there is a field with the {@link AExcelField} annotation
	 */
	private void checkFieldsKey() {

		if (!checkExcelFieldsKey) {
			return;
		}

		List<Field> queueItemFieldKey = AnnotationUtil.getFieldsWithAnnotation(getTClass(), AExcelFieldKey.class);

		if (CollectionUtils.isEmpty(queueItemFieldKey)) {
			throw new JidokaFatalException("A field must be indicated with AExcelFieldKey");
		}

		if (queueItemFieldKey.size() > 1) {
			throw new JidokaFatalException("Only one field must be entered with AExcelFieldKey");
		}
	}

	/**
	 * Maps the given {@code field}.
	 * 
	 * @param data     {@link IExcel} instance
	 * @param rowIndex Index of the row to map
	 * @param row      Row to map
	 * @param field    Field to fill
	 * @return The object resulting from mapping the {@code field}
	 */
	private T mapField(IExcel data, int rowIndex, T row, Field field) {

		T result = row;

		Object valueFromExcel = getValueFromExcel(data, rowIndex, field);

		if (valueFromExcel == null) {

			return result;
		}

		if (result == null) {

			try {

				result = getTClass().getDeclaredConstructor().newInstance();

			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new JidokaFatalException("The field cannot be mapped: " + field.getName(), e);
			}
		}

		try {

			AnnotationUtil.setFieldValue(result, field.getName(), valueFromExcel);

		} catch (JidokaException e) {
			throw new JidokaFatalException("An error occurred when setting the value of the Excel file", e);
		}

		return result;
	}

	/**
	 * Gets the value of the column given by the {@code field} in the given
	 * {@code rowIndex}
	 * 
	 * @param data
	 * @param rowIndex
	 * @param field
	 * @return The value of the given column in the given row
	 */
	private Object getValueFromExcel(IExcel data, int rowIndex, Field field) {

		AExcelField annotation = field.getAnnotation(AExcelField.class);
		String columnName = getFieldNameRegExp(annotation);

		Integer columnIndex = getColumnIndex(columnName);

		if (columnIndex == null) {
			throw new JidokaFatalException("The column couldn't be found: " + columnName);
		}

		Object value = null;

		CellReference cellRef = new CellReference(rowIndex, columnIndex);
		Cell cell = data.getCell(cellRef);

		if (!String.class.equals(field.getType())) {

			if (cell != null && CellType.NUMERIC.equals(CellType.FORMULA)) {
				cell.setCellFormula("");
			}

			value = data.getCellValueAsString(rowIndex, columnIndex);

			if (value != null && StringUtils.isBlank(String.valueOf(value))) {
				value = null;
			}

		} else {

			value = data.getCellValueAsString(rowIndex, columnIndex);
		}

		if (value instanceof String) {

			return clearString((String) value);
		}

		return value;
	}

	/**
	 * Cleans up the given {@code text}
	 * 
	 * @param text
	 * @return The cleaned up text
	 */
	private String clearString(String text) {

		return StringUtils.replaceEach(text, new String[] { " ", "–", "’" }, new String[] { " ", "-", "'" });
	}

	/**
	 * Inits the Titles position map
	 */
	private void initTitlePositionMap() {

		List<Field> excelFields = AnnotationUtil.getFieldsWithAnnotation(getTClass(), AExcelField.class);

		for (Field field : excelFields) {

			AExcelField annotation = field.getAnnotation(AExcelField.class);
			String columnNameRegExp = getFieldNameRegExp(annotation);

			titlePositionMap.put(Pair.of(columnNameRegExp, 1), null);
		}
	}

	/**
	 * Localizes the columns of the excel.
	 * 
	 * @param data
	 * @param titleRowIndex
	 * @return The list of {@link Cell} with the columns location
	 */
	private List<Pair<String, Integer>> localizeExcelColumns(IExcel data, int titleRowIndex) {

		List<Cell> cellList = data.getEntireRow(titleRowIndex);

		if (cellList.isEmpty()) {
			throw new JidokaFatalException("Couldn't find the title row");
		}

		return cellList.stream().map(cell -> Pair.of(cell.getStringCellValue(), cell.getColumnIndex()))
				.collect(Collectors.toList());
	}

	/**
	 * Calculates the title position map from the given {@code excelColumns} columns
	 * 
	 * @param excelColumns List of excel columns with its name and position
	 */
	private void calcTitlePositionMap(List<Pair<String, Integer>> excelColumns) {

		for (Pair<String, Integer> fieldKey : titlePositionMap.keySet()) {

			List<Pair<String, Integer>> matches = excelColumns.stream()
					.filter(t -> Pattern.matches(fieldKey.getLeft(), t.getLeft())).collect(Collectors.toList());

			// The field doesn't exist in the Excel (it could be created later).
			if (matches.size() < fieldKey.getRight()) {
				continue;
			}

			Integer position = matches.get(fieldKey.getRight() - 1).getRight();

			titlePositionMap.put(fieldKey, position);
		}
	}

	/**
	 * Returns the column name of the field with the given {@code annotation}.
	 * 
	 * @param annotation
	 * @return The column name
	 */
	private String getFieldNameRegExp(AExcelField annotation) {

		return annotation.fieldName();
	}

	/**
	 * Returns the column index of the column with the given {@code title}.
	 * 
	 * @param title Column title
	 * @return Teh column index
	 */
	private Integer getColumnIndex(String title) {

		return titlePositionMap.entrySet().stream().filter(t -> StringUtils.equals(title, t.getKey().getLeft()))
				.findFirst().map(Entry<Pair<String, Integer>, Integer>::getValue).orElse(null);
	}

	/**
	 * Creates the required columns if they don't exist.
	 * 
	 * @param data          IExcel instance
	 * @param titleRowIndex Title row index
	 */
	private void initCreateColumns(IExcel data, int titleRowIndex) {

		List<Field> excelFields = AnnotationUtil.getFieldsWithAnnotation(getTClass(), AExcelField.class);

		List<String> columnsToAdd = new ArrayList<>();

		for (Field field : excelFields) {

			AExcelField annotation = field.getAnnotation(AExcelField.class);

			if (!annotation.createColumn()) {
				continue;
			}

			String title = annotation.fieldName();

			columnsToAdd.add(title);
		}

		if (CollectionUtils.isNotEmpty(columnsToAdd)) {

			columnCreator.createColumns(data, columnsToAdd, titleRowIndex);
			initializeMapper(data);
		}
	}

}