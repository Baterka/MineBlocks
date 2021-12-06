package eu.d0by.utils;

import eu.d0by.utils.color.IridiumColorAPI;

import java.util.List;
import java.util.stream.Collectors;

public class Common {

	public static String colorize(String string) {
		return IridiumColorAPI.process(string);
	}

	public static List<String> colorize(List<String> list) {
		return list.stream().map(Common::colorize).collect(Collectors.toList());
	}

}
