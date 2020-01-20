import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectReplacer {

	public static List<Field> getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();
		for (Class<?> c = type; c != null; c = c.getSuperclass()) {
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		return fields;
	}

	// Replaces all object with class "targetClass" inside "current" object, with
	// new object of class "newClass", note that distinction of objec will be
	// preserved.
	public static boolean areClassesEqual(Class c1, Class c2) {
		return c1 != null && c2 != null && c1.getName().endsWith(c2.getName());
	}

	public static void replaceAllRef(Object current, Class targetClass, Class newClass,
			HashMap<Object, Object> objectForObject, HashMap<Object, Set<Field>> alreadyChecked)
			throws IllegalArgumentException, IllegalAccessException, InstantiationException {

		if (current == null)
			return;

		if (objectForObject == null)
			objectForObject = new HashMap<>();

		if (alreadyChecked == null)
			alreadyChecked = new HashMap<>();

		for (Field f : getAllFields(current.getClass())) {
			f.setAccessible(true);

			Object fieldValue = f.get(current);

			if (fieldValue != null && fieldValue.getClass() != null && fieldValue.getClass().isArray()
					&& fieldValue instanceof Object[]) {

				Object elements[] = (Object[]) fieldValue;

				for (int i = 0; i < elements.length; i++) {
					Object element = elements[i];
					if (element != null && areClassesEqual(element.getClass(), targetClass)) {
						if (!objectForObject.containsKey(element)) {
							objectForObject.put(element, newClass.newInstance());
						}

						elements[i] = objectForObject.get(element);
					} else {
						replaceAllRef(element, targetClass, newClass, objectForObject, alreadyChecked);
					}

				}

			} else {
				// ne znam da li postoji nacin da se ovo uradi efektnije

				// izbegavaj proveru istog polja na istom objektu
				if (alreadyChecked.containsKey(current) && alreadyChecked.get(current).contains(f))
					continue;

				// ne ulazimo u objekat da menjamo njegove unturasnje reference, samo stvari
				// koji pokazuju na njega
				// ali mozemo da npr. da kopiramo polja - kao npr. transform i ostale reference
				// na assete ako je objekat promenjen u editoru

				if (areClassesEqual(current.getClass(), targetClass))
					continue;

				if (!alreadyChecked.containsKey(current))
					alreadyChecked.put(current, new HashSet<Field>());

				alreadyChecked.get(current).add(f);

				// ako se klasa poklapa, nadji zamenu u mapi pokazivaca, a ako je nema, kreiraj
				// novi objekat
				if (fieldValue != null && fieldValue.getClass() != null
						&& areClassesEqual(fieldValue.getClass(), targetClass)) {

					if (!objectForObject.containsKey(fieldValue)) {
						objectForObject.put(fieldValue, newClass.newInstance());
					}

					f.set(current, objectForObject.get(fieldValue));
				} else
					replaceAllRef(fieldValue, targetClass, newClass, objectForObject, alreadyChecked);
			}

		}

	}
}
