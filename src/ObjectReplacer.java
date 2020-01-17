import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectReplacer {

	//Menja objekat Target sa objektom newRef, rekurzivno gde je Current Glava stabla
	// Replaces all "target" object inside "current" object with new object "newRef"
	public static void replaceAllRef(Object current, Object target, Object newRef, HashSet<Object> alreadyChecked)
			throws IllegalArgumentException, IllegalAccessException {
		if (current != null) {

			for (Field f : getAllFields(current.getClass())) {
				f.setAccessible(true);

				Object fieldValue = f.get(current);

				// ako je obradjeno ili setovano preskoci
				// skip if already handled or set
				if (alreadyChecked.contains(fieldValue) || fieldValue == newRef) {
					System.out.println("Hash map " + fieldValue.toString());
					System.out.println(alreadyChecked.contains(fieldValue) + "contains");
					continue;
				}

				alreadyChecked.add(fieldValue);

				// proveri svu decu
				// check for all children
				replaceAllRef(fieldValue, target, newRef, alreadyChecked);

				if (fieldValue == target)
					f.set(current, newRef);

			}

		}
	}

	public static List<Field> getAllFields(Class<?> type) {
		List<Field> fields = new ArrayList<Field>();
		for (Class<?> c = type; c != null; c = c.getSuperclass()) {
			fields.addAll(Arrays.asList(c.getDeclaredFields()));
		}
		return fields;
	}

	
	// Replaces all object with class "targetClass" inside "current" object, with
	// new object of class "newClass", note that distinction of objec will be preserved. 
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
					if (element != null && element.getClass().equals(targetClass)) {
						if (!objectForObject.containsKey(element)) {
							objectForObject.put(element, newClass.newInstance());
						}

						elements[i] = objectForObject.get(element);
					} else {
						replaceAllRef(element, targetClass, newClass, objectForObject, alreadyChecked);
					}

				}

			} else {
				
				// izbegavaj proveru istog polja na istom objektu
				//avoid double checking
				if (alreadyChecked.containsKey(current) && alreadyChecked.get(current).contains(f))
					continue;

				//preskoci ako je klasa vec setovana
				//skip if class is alread correct 	
				if (current.getClass() == targetClass)
					continue;

				//obelezi provereno polje
				//mark that field is checked
				if (!alreadyChecked.containsKey(current))
					alreadyChecked.put(current, new HashSet<Field>());

				alreadyChecked.get(current).add(f);

				// ako se klasa poklapa, nadji zamenu u mapi pokazivaca, a ako je nema, kreiraj novi objekat
				// if classes are matching, find a replacement of an object inside map, if not present create a new object  
				if (fieldValue != null && fieldValue.getClass() != null && fieldValue.getClass().equals(targetClass)) {

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
