/**
 * Copyright (c) 2016 
 * CORE - CESAR ORE.
 */
package com.coresware.utils.textfile.generator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

/**
 * Descripción: Esta clase es usada para generar datasources desde un archivo
 * csv. Clase creada el Oct 23, 2016.
 * 
 * @author CESAR ORE [core@coresware.com].
 */
public class Main {

	/**
	 * Usado para generar los datasources. Se requieren 4 parámetros:<BR>
	 * <BR>
	 * - CSVORIGEN: Donde se encuentra la información para generar los archivos.
	 * <BR>
	 * - PLANTILLAORIGEN: Archivo base a partir del cual se reemplazarán las
	 * cadenas que corresponden a los atributos en el CSV origen. <BR>
	 * - CARPETADESTINO: Donde se depositarán los archivos generados. <BR>
	 * - NOMBREGENERADOS: Nombre final que tendrán los archivos generados, se
	 * reemplazarán las cadenas que corresponden a los atributos en el CSV.<BR>
	 * <BR>
	 * Retorna un objeto del tipo void. Método creado el Oct 23, 2016, por CESAR
	 * ORE [core@coresware.com].
	 * 
	 * @param args
	 *            Argumentos de la aplicación: CSVORIGEN, PLANTILLAORIGEN,
	 *            CARPETADESTINO y NOMBREGENERADOS.
	 */
	public static void main(String[] args) {
		// validar parámetros
		if (args.length != 4) {
			Main.printParamsAndExit(-1);
		}

		String strCsvOrigen = args[0];
		String strPlantillaOrigen = args[1];
		String strCarpetaDestino = args[2];
		String strNombreGenerados = args[3];

		Main.printVariables(strCsvOrigen, strPlantillaOrigen, strCarpetaDestino, strNombreGenerados);

		File csvOrigen = new File(strCsvOrigen);
		File plantillaOrigen = new File(strPlantillaOrigen);
		File carpetaDestino = new File(strCarpetaDestino);

		if (!csvOrigen.isFile() || !Main.getExtension(csvOrigen.getName()).toUpperCase().equals("CSV")) {
			Main.printMessageAndExit("CSVORIGEN no es un archivo válido.", -1);
		}

		if (!plantillaOrigen.isFile()) {
			Main.printMessageAndExit("PLANTILLAORIGEN no es un archivo válido.", -1);
		}

		if (!carpetaDestino.isDirectory()) {
			Main.printMessageAndExit("DESTINO no es un directorio válido.", -1);
		}

		String plantilla = null;
		try {
			plantilla = new String(Files.readAllBytes(Paths.get(strPlantillaOrigen)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			Main.printMessageAndExit("Error al leer archivo " + strPlantillaOrigen, -1);
		}

		List<Map<String, String>> datos = null;
		try {
			datos = Main.readCSV(csvOrigen);
		} catch (Exception e) {
			e.printStackTrace();
			Main.printMessageAndExit("Error al leer archivo " + strCsvOrigen, -1);
		}

		if (plantilla == null || plantilla.trim().isEmpty() || datos == null || datos.isEmpty()) {
			Main.printMessageAndExit("No hay datos suficientes para generar los archivos.", -1);
		}

		for (Map<String, String> dato : datos) {
			String content = Main.replaceData(plantilla, dato);
			String resultFilePath = strCarpetaDestino + File.separator + Main.replaceData(strNombreGenerados, dato);

			try {
				Files.write(Paths.get(resultFilePath), content.getBytes(StandardCharsets.UTF_8),
						StandardOpenOption.CREATE);
			} catch (IOException e) {
				// Sólo informar que archivo falló, no cancelar la ejecución del
				// resto.
				System.err.println("Error al crear archivo [" + resultFilePath + "]: " + e.getMessage());
			}

		}

		Main.printMessageAndExit("Ejecución terminada.", 0);

	}

	/**
	 * Usado para imprimir los parámetros con los que se ejecuta el programa.
	 * Método creado el Oct 23, 2016, por CESAR ORE [core@coresware.com].
	 * 
	 * @param strCsvOrigen
	 *            Donde se encuentra la información para generar los archivos.
	 * @param strPlantillaOrigen
	 *            Archivo base a partir del cual se reemplazarán las cadenas que
	 *            corresponden a los atributos en el CSV origen.
	 * @param strCarpetaDestino
	 *            Donde se depositarán los archivos generados.
	 * @param strNombreGenerados
	 *            Nombre final que tendrán los archivos generados, se
	 *            reemplazarán las cadenas que corresponden a los atributos en
	 *            el CSV.
	 */
	private static void printVariables(String strCsvOrigen, String strPlantillaOrigen, String strCarpetaDestino,
			String strNombreGenerados) {
		System.out.println();
		System.out.println();
		System.out.println(String.format("%-8s: %s", "EJECUTANDO", ""));
		System.out.println(String.format("%-8s: %s", "CSVORIGEN", strCsvOrigen));
		System.out.println(String.format("%-8s: %s", "PLANTILLAORIGEN", strPlantillaOrigen));
		System.out.println(String.format("%-8s: %s", "CARPETADESTINO", strCarpetaDestino));
		System.out.println(String.format("%-8s: %s", "NOMBREGENERADOS", strNombreGenerados));
		System.out.println();
		System.out.println();

	}

	/**
	 * Usado para imprimir los parámetros de funcionamiento del programa y
	 * terminar el programa. Retorna un objeto del tipo void. Método creado el
	 * Oct 23, 2016, por CESAR ORE [core@coresware.com].
	 * 
	 * @param exitCode
	 *            Código de salida.
	 */
	private static void printParamsAndExit(int exitCode) {
		System.out.println();
		System.out.println();
		System.out.println(String.format("%-8s: %s", "ERROR",
				"Parámetros incorrectos, se requieren 4 parámetros: CSVORIGEN, PLANTILLAORIGEN, CARPETADESTINO y NOMBREGENERADOS."));
		System.out.println();
		System.out.println(String.format("%-8s: %s", "EJEMPLO", ""));
		System.out.println(String.format("%-2s  %s", "",
				"java -jar /rutagenerador/textfile-generator-1.0.0-jar-with-dependencies.jar /ruta-csv/archivo.csv /ruta-templ/template.txt /ruta-destino/ archivo_ATRIB_.txt"));
		System.out.println();
		System.out.println(String.format("%-8s: %s", "DONDE", ""));
		System.out.println(String.format("%-24s %s", "/ruta-csv/archivo.csv",
				"(CSVORIGEN) Ruta donde se encuentra el CSVORIGEN con la información dinámica a usar."));
		System.out.println(String.format("%-24s %s", "/ruta-templ/template.txt",
				"(PLANTILLAORIGEN) Plantilla a la que se le reemplazarán las cadenas que corresponden a los valores contenidos en el CSV."));
		System.out.println(String.format("%-24s %s", "/ruta-destino/",
				"(CARPETADESTINO) Carpeta donde se depositarán los archivos generados. La carpeta debe existir."));
		System.out.println(String.format("%-24s %s", "archivo_ATRIB_.txt",
				"(NOMBREGENERADOS) Nombre para los archivos a generar, las partes del texto que coincidan con el nombre de alguna columna del CSV serán reemplazadas con ese valor."));
		System.out.println(String.format("%-24s %s", "",
				"En este ejemplo '_ATRIB_' corresponde a una columna en el CSV y su valor se reemplaza para crear los nombres de los archivos generados."));
		System.out.println();
		System.out.println();
		System.exit(exitCode);
	}

	/**
	 * Usado para imprimir mensaje de error y terminar el programa. Un código de
	 * salida menor a cero indica error. Si es mayor o igual a cero es un caso
	 * de éxito. Retorna un objeto del tipo void. Método creado el Oct 23, 2016,
	 * por CESAR ORE [core@coresware.com].
	 * 
	 * @param message
	 *            Mensaje.
	 * @param exitCode
	 *            Código de salida.
	 */
	private static void printMessageAndExit(String message, int exitCode) {

		if (exitCode >= 0) {
			// Mayor o igual a cero significa sin error!
			System.out.println(message);
		} else {
			// Menor a cero, es mensaje de error!!
			System.err.println(message);
		}

		System.exit(exitCode);
	}

	/**
	 * Usado para obtener la extensión de un archivo. Retorna un objeto del tipo
	 * String. Método creado el Oct 23, 2016, por CESAR ORE
	 * [core@coresware.com].
	 * 
	 * @param filename
	 *            String
	 * @return String
	 */
	private static String getExtension(String filename) {
		return filename.substring(filename.lastIndexOf(".") + 1, filename.length());
	}

	/**
	 * 
	 * Usado para leer un CSV. Retorna un objeto del tipo
	 * List<Map<String,String>>. Método creado el Oct 23, 2016, por CESAR ORE
	 * [core@coresware.com].
	 * 
	 * @param file
	 *            El archivo a leer.
	 * @return List<Map<String,String>>
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private static List<Map<String, String>> readCSV(File file) throws JsonProcessingException, IOException {
		List<Map<String, String>> result = new LinkedList<Map<String, String>>();
		CsvMapper mapper = new CsvMapper();
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		@SuppressWarnings("deprecation")
		MappingIterator<Map<String, String>> iterator = mapper.reader(Map.class).with(schema).readValues(file);
		while (iterator.hasNext()) {
			result.add(iterator.next());
		}
		return result;
	}

	/**
	 * Usado para reemplazar datos en una cadena de texto. Retorna un objeto del
	 * tipo String. Método creado el Oct 23, 2016, por CESAR ORE
	 * [core@coresware.com].
	 * 
	 * @param template
	 *            El texto donde se apolicarán los reemplazos.
	 * @param data
	 *            Mapa de datos a reemplazar.
	 * @return String
	 */
	private static String replaceData(String template, Map<String, String> data) {
		String result = template;

		for (String key : data.keySet()) {
			String value = data.get(key);
			result = result.replaceAll(key, value);
		}

		return result;
	}

}
