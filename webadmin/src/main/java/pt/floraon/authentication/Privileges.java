package pt.floraon.authentication;

import pt.floraon.authentication.entities.User;

import java.util.*;

/**
 * Created by miguel on 06-01-2017.
 */
public enum Privileges {
    VIEW_FULL_SHEET(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , VIEW_OCCURRENCES(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EXCLUDE_OCCURRENCES(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , MODIFY_OCCURRENCES(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , DOWNLOAD_OCCURRENCES(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_SECTION2(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_SECTION3(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_SECTION4(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_SECTION5(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_SECTION6(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_SECTION7(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_SECTION8(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_ALL_TEXTUAL(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_1_4(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_9_4_9_7(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_9_1_2_3_5(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_9_6_8_41(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_9_9_1(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_9_9_2(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_9_9_3(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , EDIT_9_9_4(User.PrivilegeType.REDLISTDATA, User.PrivilegeScope.PER_SPECIES)
    , CREATE_REDLIST_DATASETS(User.PrivilegeType.GLOBAL, User.PrivilegeScope.GLOBAL)
    , MODIFY_TAXA_TERRITORIES(User.PrivilegeType.CHECKLIST, User.PrivilegeScope.GLOBAL)
    , EDIT_FULL_CHECKLIST(User.PrivilegeType.CHECKLIST, User.PrivilegeScope.GLOBAL)
    , MANAGE_REDLIST_USERS(User.PrivilegeType.GLOBAL, User.PrivilegeScope.GLOBAL);

    private User.PrivilegeType privilegeType;
    private User.PrivilegeScope privilegeScope;

    Privileges(User.PrivilegeType pt, User.PrivilegeScope ps) {
        this.privilegeType = pt;
        this.privilegeScope = ps;
    }

    public User.PrivilegeType getPrivilegeType() {
        return this.privilegeType;
    }

    public User.PrivilegeScope getPrivilegeScope() {
        return this.privilegeScope;
    }

    public static class PrivilegeNameComparator implements Comparator<Privileges> {
        public int compare(Privileges o1, Privileges o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }

    public static List<Privileges> getAllPrivilegesOfType(User.PrivilegeType type) {
        List<Privileges> out = new ArrayList<>();
        if(type == null) {
            out = Arrays.asList(Privileges.values());
            Collections.sort(out, new PrivilegeNameComparator());
            return out;
        }

        for (Privileges priv : Privileges.values()) {
            if(priv.getPrivilegeType() == type) out.add(priv);
        }
        Collections.sort(out, new PrivilegeNameComparator());
        return out;
    }

}
