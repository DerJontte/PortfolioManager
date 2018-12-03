package uppgift3.model;

// Denna klass gör det enklare att avrunda flyttal till ett visst antal decimaler. Den gör heller inte konstiga avrundningsfel
// som Math.round() gör.

public class RoundFix {
	public static double setDecimals(int decimals, String inValue) {
		return setDecimals(decimals, Double.parseDouble(inValue));
	}
	
	public static double setDecimals(int decimals, double inValue) {
		String format = "%." + decimals + "f";
		String formatted = String.format(format, inValue);
		formatted = formatted.replace(",", ".");
		return Double.valueOf(formatted);
	}
}
