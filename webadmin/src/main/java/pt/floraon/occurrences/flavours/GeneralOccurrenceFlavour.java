package pt.floraon.occurrences.flavours;

import pt.floraon.driver.annotations.*;
import pt.floraon.driver.datatypes.IntegerInterval;
import pt.floraon.driver.utils.StringUtils;
import pt.floraon.occurrences.entities.Inventory;
import pt.floraon.occurrences.entities.OBSERVED_IN;
import pt.floraon.occurrences.entities.SpecialFields;
import pt.floraon.redlistdata.RedListEnums;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// TODO: Inventory fields
public abstract class GeneralOccurrenceFlavour implements IOccurrenceFlavour {

    private Field findField(String field) {
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
                    e2.printStackTrace();
                    return null;
                }
            }
        }
        return f;
    }

    @Override
    public String getFieldName(String field) {
        Field f = findField(field);
        if(f == null) return "??" + field + "??";

        PrettyName obsField = f.getAnnotation(PrettyName.class);
        return (obsField == null || StringUtils.isStringEmpty(obsField.value())) ? field : obsField.value();
    }

    @Override
    public String getFieldValue(OBSERVED_IN occurrence, Inventory inventory, String field) {
        Method method;
        Object result = null;
        String methodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
        if(isInventoryField(field)) {
            try {
                method = Inventory.class.getMethod(methodName);
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
                return "";
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
                return "";
            }
            try {
                result = method.invoke(occurrence);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }


        if(result == null) return "";

        if(RedListEnums.LabelledEnum.class.isInstance(result))
            return ((RedListEnums.LabelledEnum) result).getLabel();
        else if(IntegerInterval.class.isInstance(result)) {
            if(((IntegerInterval) result).getError() != null)
                return "<span class=\"error\">" + result.toString() + "</span>";
            else
                return result.toString();
        } else
            return result.toString();
    }

    @Override
    public String getFieldShortName(String field) {
        Field f = findField(field);
        if(f == null) return "??" + field + "??";
        PrettyName obsField = f.getAnnotation(PrettyName.class);
        return (obsField == null || StringUtils.isStringEmpty(obsField.value())) ? field : obsField.shortName();
    }

    @Override
    public boolean hideFieldInCompactView(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(HideInCompactView.class);
    }


    @Override
    public boolean isInventoryField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(HideInInventoryView.class);

/*
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
                    e2.printStackTrace();
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
*/
    }

    @Override
    public boolean isReadOnly(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(ReadOnly.class);
    }

    @Override
    public boolean hideFieldInInventoryView(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(HideInInventoryView.class);
    }

    @Override
    public boolean isSmallField(String field) {
        Field f = findField(field);
        if(f == null) return false;
        return f.isAnnotationPresent(SmallField.class);
    }

}
