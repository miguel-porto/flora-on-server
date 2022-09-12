package pt.floraon.occurrences.fields;

import jdk.internal.org.jline.utils.Log;
import pt.floraon.driver.annotations.*;
import pt.floraon.driver.datatypes.IntegerInterval;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.entities.SpecialFields;
import pt.floraon.occurrences.fields.parsers.BooleanParser;
import pt.floraon.redlistdata.RedListEnums;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class FieldReflection {
    static public Field findField(String field) {
        // TODO cache fields upon start
        Field f;
        try {
            f = OBSERVED_IN.class.getDeclaredField(field);
            if(!f.isAnnotationPresent(PrettyName.class)) throw new NoSuchFieldException();
        } catch (NoSuchFieldException e) {
            try {
                f = Inventory.class.getDeclaredField(field);
                if(!f.isAnnotationPresent(PrettyName.class)) throw new NoSuchFieldException();
            } catch (NoSuchFieldException e1) {
                try {
                    f = SpecialFields.class.getDeclaredField(field);
                    if(!f.isAnnotationPresent(PrettyName.class)) throw new NoSuchFieldException();
                } catch (NoSuchFieldException e2) {
                    return null;
                }
            }
        }
        return f;
    }

    static public void setFieldValueString(Inventory inventory, String field, String value)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method;
        String methodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
        if(isInventoryField(field)) {
            method = Inventory.class.getMethod(methodName, String.class);
            method.invoke(inventory, value);
        } else {
            method = OBSERVED_IN.class.getMethod(methodName, String.class);

            if(inventory.getUnmatchedOccurrences().size() == 0)
                inventory.getUnmatchedOccurrences().add(new OBSERVED_IN(true));

            for(OBSERVED_IN obs : inventory.getUnmatchedOccurrences()) {
                method.invoke(obs, value);
            }
        }
    }

    static public void setFieldValueInteger(Inventory inventory, String field, Integer value)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method;
        String methodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
        if(isInventoryField(field)) {
            method = Inventory.class.getMethod(methodName, Integer.class);
            method.invoke(inventory, value);
        } else {
            method = OBSERVED_IN.class.getMethod(methodName, Integer.class);

            if(inventory.getUnmatchedOccurrences().size() == 0)
                inventory.getUnmatchedOccurrences().add(new OBSERVED_IN(true));

            for(OBSERVED_IN obs : inventory.getUnmatchedOccurrences()) {
                method.invoke(obs, value);
            }
        }
    }

    static public <T> void setFieldValueAny (Inventory inventory, String field, Class type, T value)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method method;
        String methodName = "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
        if(isInventoryField(field)) {
            method = Inventory.class.getMethod(methodName, type);
            method.invoke(inventory, value);
        } else {
            method = OBSERVED_IN.class.getMethod(methodName, type);

            if(inventory.getUnmatchedOccurrences().size() == 0)
                inventory.getUnmatchedOccurrences().add(new OBSERVED_IN(true));

            for(OBSERVED_IN obs : inventory.getUnmatchedOccurrences()) {
                method.invoke(obs, value);
            }
        }
    }

    private static Object getFieldValueInternal(OBSERVED_IN occurrence, Inventory inventory, String field) {
        Method method;
        Object result = null;
        String methodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);

        if(isSpecialField(field)) {
            try {
                method = SpecialFields.class.getMethod(methodName, Inventory.class, OBSERVED_IN.class);
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
                return "<error>";
            }
            try {
                result = method.invoke(null, inventory, occurrence);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else if (isInventoryField(field)) {
            try {
                method = Inventory.class.getMethod(methodName);
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
                return "<error>";
            }
            try {
                result = method.invoke(inventory);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            try {
                method = OBSERVED_IN.class.getMethod(methodName);
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
                return "<error>";
            }
            try {
                result = method.invoke(occurrence);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Gets the display value of any field, coercing any types to a human-readable string
     * @param occurrence
     * @param inventory
     * @param field
     * @return
     */
    static public String getFieldValue(OBSERVED_IN occurrence, Inventory inventory, String field) {
        Object result = FieldReflection.getFieldValueInternal(occurrence, inventory, field);

        if(result == null) return "";

        // Some processing of the result, in special cases.
        if(result instanceof RedListEnums.LabelledEnum)
            return ((RedListEnums.LabelledEnum) result).getLabel();
        else if(result instanceof IntegerInterval) {
            if(((IntegerInterval) result).getError() != null)
                return "<span class=\"error\">" + result.toString() + "</span>";
            else
                return result.toString();
        } else if(result instanceof String[]) {
            return StringUtils.implode(", ", (String[]) result);
        } else if(result instanceof Boolean) {
            return ((Boolean) result) ? "Yes" : "No";
        } else if(isBooleanField(field)) {
            Object o = (new BooleanParser()).preProcessValue(result.toString());
            return o == null ? "" : ((Boolean) o ? "Yes" : "No");
        } else
            return result.toString();
    }

    /**
     * Gets the value of any field, but does not do any post-processing. The field type is returned.
     * @param occurrence
     * @param inventory
     * @param field
     * @return
     */
    static public Object getFieldValueRaw(OBSERVED_IN occurrence, Inventory inventory, String field) {
        Object result = FieldReflection.getFieldValueInternal(occurrence, inventory, field);
        return result;
    }

    static public boolean isInventoryField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(InventoryField.class);

    }

    static public boolean isSpecialField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(SpecialField.class);

    }

    static public boolean isReadOnly(String field, boolean isAdmin) {
        Field f = findField(field);
        if(f == null) return false;
        if(f.isAnnotationPresent(ReadOnly.class)) {
            return !f.getAnnotation(ReadOnly.class).adminCanEdit() || !isAdmin;
        }
            else return false;
//        return f.isAnnotationPresent(ReadOnly.class);
    }

    static public boolean isImageField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(FieldType.class) && f.getAnnotation(FieldType.class).value() == FieldType.Type.IMAGE;
    }

    static public boolean isAuthorField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(FieldType.class) && f.getAnnotation(FieldType.class).value() == FieldType.Type.AUTHORS;
    }

    static public boolean isDateField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(FieldType.class) && f.getAnnotation(FieldType.class).value() == FieldType.Type.DATE;
    }

    static public boolean isBooleanField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(FieldType.class) && f.getAnnotation(FieldType.class).value() == FieldType.Type.BOOLEAN;
    }

    static public String getFieldSize(String field) {
        Field f = findField(field);
        if(f == null) return FieldStyle.Size.BIG.toString();
        return f.isAnnotationPresent(FieldStyle.class) ? f.getAnnotation(FieldStyle.class).value().toString() : FieldStyle.Size.BIG.toString();
    }

    static public boolean isImportantField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        if(f.isAnnotationPresent(PrettyName.class))
            return f.getAnnotation(PrettyName.class).important();
        else
            return false;
    }

    static public String[] getDatabaseFields(String field) {
        Field f = findField(field);
        if(f == null) return new String[0];
        if(f.isAnnotationPresent(DatabaseFields.class)) {
            return f.getAnnotation(DatabaseFields.class).value();
        } else
            return new String[] {field};
    }

    static public boolean hideFieldInCompactView(String field) {
        return false;
/*
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(HideInCompactView.class);
*/
    }

    static public String getFieldShortName(String field, boolean advanced) {
        Field f = findField(field);
        if(f == null) return "??" + field + "??";
        PrettyName obsField = f.getAnnotation(PrettyName.class);
        if(obsField == null) return field;

        String ret;
        if(advanced)
            ret = obsField.shortNameAdvanced().equals("") ? obsField.shortName() : obsField.shortNameAdvanced();
        else
            ret = obsField.shortName();

        return StringUtils.isStringEmpty(ret) ? getFieldName(field, advanced) : ret;
    }

    static public String getFieldName(String field, boolean advanced) {
        Field f = findField(field);
        if(f == null) return "??" + field + "??";

        PrettyName obsField = f.getAnnotation(PrettyName.class);
        if(obsField == null) return field;
        String ret;
        if(advanced)
            ret = obsField.nameAdvanced().equals("") ? obsField.value() : obsField.nameAdvanced();
        else
            ret = obsField.value();

        return StringUtils.isStringEmpty(ret) ? field : ret;
    }

    static public String[] getFieldValues(String field, boolean advanced) {
        Field f = findField(field);
        if(f == null) return new String[0];
        if(f.isAnnotationPresent(EditWidget.class)) {
            EditWidget a = f.getAnnotation(EditWidget.class);
            return advanced ? (a.valuesAdvanced().length == 0 ? a.valuesSimple() : a.valuesAdvanced()) : a.valuesSimple();
        } else
            return new String[0];
    }

    static public String[] getFieldLabels(String field, boolean advanced) {
        Field f = findField(field);
        if(f == null) return new String[0];
        if(f.isAnnotationPresent(EditWidget.class)) {
            EditWidget a = f.getAnnotation(EditWidget.class);
            return advanced ? (a.labelsAdvanced().length == 0 ? a.labelsSimple() : a.labelsAdvanced()) : a.labelsSimple();
        } else
            return new String[0];
    }

    static public EditWidget.Type getFieldWidget(String field, boolean advanced) {
        Field f = findField(field);
        if(f == null) return null;
        if(!f.isAnnotationPresent(EditWidget.class)) return EditWidget.Type.TEXT;
        EditWidget a = f.getAnnotation(EditWidget.class);
        return advanced ? (a.widgetAdvanced() == EditWidget.Type.NULL ? a.value() : a.widgetAdvanced()) : a.value();
    }

    static public boolean isMonospaceFont(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(FieldStyle.class) && f.getAnnotation(FieldStyle.class).monospaceFont();
    }

    static public boolean breakLines(String field) {
        Field f = findField(field);
        if(f == null || !f.isAnnotationPresent(FieldStyle.class)) return true;
        return f.getAnnotation(FieldStyle.class).breakLines();
    }

    static public boolean isBigEditWidget(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(EditWidget.class) && f.getAnnotation(EditWidget.class).value() == EditWidget.Type.BIGTEXT;
    }

    static public boolean isAdminField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(AdminOnly.class);
    }

    static public Class<? extends pt.floraon.driver.parsers.FieldParser> getFieldParser(String field) {
        Field f = findField(field);
        if(f == null) return null;
        if(f.isAnnotationPresent(pt.floraon.driver.annotations.FieldParser.class)) {
            return f.getAnnotation(FieldParser.class).value();
        } else return null;
    }

}
