package org.drools.brms.server.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mvel.MVEL;

/**
 * Use mvel to load up map/list of valid items for fields - used by the Guided rule editor.
 */
public class DataEnumLoader {

	private final List errors;
	private final Map data;

	/**
	 * This is the source of the asset, which is an MVEL map (minus the outer "[") of course.
	 */
	public DataEnumLoader(String mvelSource) {
		errors = new ArrayList();
		this.data = loadEnum(mvelSource);
	}

	private Map loadEnum(String mvelSource) {
		final Object mvelData;
		try {
			MVEL mvel = new MVEL();

			mvelData = MVEL.eval("[ " + mvelSource + " ]", new HashMap());
		} catch (RuntimeException e) {
			addError("Unable to load enumeration data.");
			addError(e.getMessage());
			addError("Error type: " + e.getClass().getName());
			return Collections.EMPTY_MAP;
		}
		if (!(mvelData instanceof Map)) {
			addError("The expression is not a map, it is a " + mvelData.getClass().getName());
			return Collections.EMPTY_MAP;
		}
		Map map = (Map) mvelData;
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			Object list = map.get(key);
			if (!(list instanceof List)) {
				if (list == null) {
					addError("The item with " + key + " is null.");
				} else {
					addError("The item with " + key + " is not a list, it is a " + list.getClass().getName());
				}
				return Collections.EMPTY_MAP;
			}
			List items = (List) list;
			List newItems = new ArrayList();
			for (int i = 0; i < items.size(); i++) {
				Object listItem = items.get(i);
				if (!(listItem instanceof String)) {
					newItems.add(listItem.toString());
				} else {
					newItems.add(listItem);
				}
			}
			map.put(key, newItems);
		}
		return map;
	}

	private void addError(String string) {
		this.errors.add(string);
	}

	/**
	 * Return a list of any errors found.
	 */
	public List getErrors() {
		return this.errors;
	}

	/**
	 * Return the map of Fact.field to List (of Strings).
	 */
	public Map getData() {
		return this.data;
	}

}
