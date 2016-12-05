package pt.floraon.authentication.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.Constants;
import pt.floraon.driver.DatabaseException;
import pt.floraon.driver.entities.NamedDBNode;
import pt.floraon.authentication.PasswordAuthentication;
import pt.floraon.occurrences.entities.Author;

import java.util.*;

import static pt.floraon.authentication.entities.User.Privileges.*;

/**
 * Represents a user of the Flora-On Server - not to be confounded with an {@link Author} of observations!
 */
public class User extends NamedDBNode {
	public enum UserType {ADMINISTRATOR, REGULAR}
	public enum PrivilegeType {CHECKLIST, REDLISTDATA, GLOBAL}

	public enum Privileges {
		VIEW_FULL_SHEET(PrivilegeType.REDLISTDATA)
		, VIEW_OCCURRENCES(PrivilegeType.REDLISTDATA)
		, EXCLUDE_OCCURRENCES(PrivilegeType.REDLISTDATA)
		, MODIFY_OCCURRENCES(PrivilegeType.REDLISTDATA)
		, DOWNLOAD_OCCURRENCES(PrivilegeType.REDLISTDATA)
		, EDIT_SECTION2(PrivilegeType.REDLISTDATA)
		, EDIT_SECTION3(PrivilegeType.REDLISTDATA)
		, EDIT_SECTION4(PrivilegeType.REDLISTDATA)
		, EDIT_SECTION5(PrivilegeType.REDLISTDATA)
		, EDIT_SECTION6(PrivilegeType.REDLISTDATA)
		, EDIT_SECTION7(PrivilegeType.REDLISTDATA)
		, EDIT_SECTION8(PrivilegeType.REDLISTDATA)
		, EDIT_ALL_TEXTUAL(PrivilegeType.REDLISTDATA)
		, EDIT_1_4(PrivilegeType.REDLISTDATA)
		, EDIT_9_4_9_7(PrivilegeType.REDLISTDATA)
		, EDIT_9_1_2_3_5(PrivilegeType.REDLISTDATA)
		, EDIT_9_6_8_41(PrivilegeType.REDLISTDATA)
		, CREATE_REDLIST_DATASETS(PrivilegeType.GLOBAL)
		, MODIFY_TAXA_TERRITORIES(PrivilegeType.CHECKLIST)
		, EDIT_FULL_CHECKLIST(PrivilegeType.CHECKLIST)
		, MANAGE_REDLIST_USERS(PrivilegeType.GLOBAL);

		private PrivilegeType privilegeType;

		Privileges(PrivilegeType pt) {
			this.privilegeType = pt;
		}

		public PrivilegeType getPrivilegeType() {
			return this.privilegeType;
		}
	}

	public static List<Privileges> getAllPrivilegesOfType(PrivilegeType type) {
		List<Privileges> out = new ArrayList<>();
		if(type == null) return Arrays.asList(Privileges.values());

		for (Privileges priv : Privileges.values()) {
			if(priv.getPrivilegeType() == type) out.add(priv);
		}
		return out;
	}

	public static Map<String, Privileges[]> userProfiles;
	static {
		userProfiles = new HashMap<>();
		userProfiles.put("AUTHOR", new Privileges[] { EDIT_1_4, EDIT_SECTION2, EDIT_SECTION3, EDIT_SECTION4
				, EDIT_SECTION5, EDIT_SECTION6, EDIT_SECTION7, EDIT_SECTION8, EDIT_9_4_9_7});
		userProfiles.put("EVALUATOR", new Privileges[] { EDIT_9_1_2_3_5});
		userProfiles.put("REVIEWER", new Privileges[] { EDIT_9_6_8_41, EDIT_9_1_2_3_5});
	}

	public static Privileges[] EDIT_SECTIONS2_8 = new Privileges[] { EDIT_SECTION2, EDIT_SECTION3, EDIT_SECTION4
		, EDIT_SECTION5, EDIT_SECTION6, EDIT_SECTION7, EDIT_SECTION8};

	public static Privileges[] EDIT_ALL_FIELDS = new Privileges[] { EDIT_SECTION2, EDIT_SECTION3, EDIT_SECTION4
			, EDIT_SECTION5, EDIT_SECTION6, EDIT_SECTION7, EDIT_SECTION8, EDIT_ALL_TEXTUAL, EDIT_1_4, EDIT_9_4_9_7
			, EDIT_9_1_2_3_5, EDIT_9_6_8_41 };

	private String userName, password;
	private UserType userType;
	private Set<Privileges> privileges = new HashSet<>();

	public String getUserName() {
		return userName;
	}

	public String getNameASCii() {
		return java.text.Normalizer.normalize(super.getName(), java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","").replace(" ", "_");
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public User() {
		super();
	}

	public User(String username, String fullName, Set<Privileges> privileges) throws DatabaseException {
		super(fullName);
		this.userName = username;
		this.privileges = privileges;
		this.userType = UserType.REGULAR;
	}

	public User(String username, String fullName, Privileges[] privileges) throws DatabaseException {
		this(username, fullName, new HashSet<>(Arrays.asList(privileges)));
	}

	public UserType getUserType() {
		return userType == null ? UserType.REGULAR : userType;
	}

	public void setUserType(String userType) {
		this.userType = UserType.valueOf(userType);
	}

	@Override
	public Constants.NodeTypes getType() {
		return Constants.NodeTypes.user;
	}

	@Override
	public String getTypeAsString() {
		return this.getType().toString();
	}

	@Override
	public JsonObject toJson() {
		return super._toJson();
	}

	@Override
	public String toJsonString() {
		return this.toJson().toString();
	}

	public Set<Privileges> getPrivileges() {return privileges;}

	public String getPassword() {
		return password;
	}

	public void clearPassword() {
		this.password = null;
	}

	public void setPassword(char[] password) {
		if(password.length > 0) {
			PasswordAuthentication pa = new PasswordAuthentication();
			this.password = pa.hash(password);
		}
	}

	public void setPrivilege(Privileges privilege, boolean value) {
		if(value)
			this.privileges.add(privilege);
		else
			this.privileges.remove(privilege);
	}

	public boolean isGuest() {
		return privileges.size() == 0;
	}

	public void setPrivileges(Privileges[] privileges) {
		this.privileges.addAll(Arrays.asList(privileges));
	}

	public void setVIEW_FULL_SHEET(boolean value) {
		setPrivilege(Privileges.VIEW_FULL_SHEET, value);
	}

	public void setVIEW_OCCURRENCES(boolean value) {
		setPrivilege(Privileges.VIEW_OCCURRENCES, value);
	}

	public void setEDIT_SECTION2(boolean value) {
		setPrivilege(EDIT_SECTION2, value);
	}

	public void setEXCLUDE_OCCURRENCES(boolean value) {
		setPrivilege(EXCLUDE_OCCURRENCES, value);
	}

	public void setMODIFY_OCCURRENCES(boolean value) {
		setPrivilege(MODIFY_OCCURRENCES, value);
	}
	public void setDOWNLOAD_OCCURRENCES(boolean value) {
		setPrivilege(DOWNLOAD_OCCURRENCES, value);
	}
	public void setEDIT_SECTION3(boolean value) {
		setPrivilege(EDIT_SECTION3, value);
	}
	public void setEDIT_SECTION4(boolean value) {
		setPrivilege(EDIT_SECTION4, value);
	}

	public void setEDIT_SECTION5(boolean value) {
		setPrivilege(EDIT_SECTION5, value);
	}

	public void setEDIT_SECTION6(boolean value) {
		setPrivilege(EDIT_SECTION6, value);
	}

	public void setEDIT_SECTION7(boolean value) {
		setPrivilege(EDIT_SECTION7, value);
	}

	public void setEDIT_SECTION8(boolean value) {
		setPrivilege(EDIT_SECTION8, value);
	}

	public void setEDIT_ALL_TEXTUAL(boolean value) {
		setPrivilege(EDIT_ALL_TEXTUAL, value);
	}

	public void setEDIT_1_4(boolean value) {
		setPrivilege(EDIT_1_4, value);
	}

	public void setEDIT_9_4_9_7(boolean value) {
		setPrivilege(EDIT_9_4_9_7, value);
	}

	public void setEDIT_9_1_2_3_5(boolean value) {
		setPrivilege(EDIT_9_1_2_3_5, value);
	}

	public void setEDIT_9_6_8_41(boolean value) {
		setPrivilege(EDIT_9_6_8_41, value);
	}

	public void setCREATE_REDLIST_DATASETS(boolean value) {
		setPrivilege(CREATE_REDLIST_DATASETS, value);
	}

	public void setMODIFY_TAXA_TERRITORIES(boolean value) {
		setPrivilege(MODIFY_TAXA_TERRITORIES, value);
	}

	public void setEDIT_FULL_CHECKLIST(boolean value) {
		setPrivilege(EDIT_FULL_CHECKLIST, value);
	}

	public void setMANAGE_REDLIST_USERS(boolean value) {
		setPrivilege(MANAGE_REDLIST_USERS, value);
	}

	public boolean hasPrivilege(Privileges privilege) {
		return this.privileges.contains(privilege);
	}

	public boolean hasAnyPrivilege(Privileges[] privileges) {
		Set<Privileges> tmp = new HashSet<>(Arrays.asList(privileges));
		tmp.retainAll(this.privileges);
		return tmp.size() > 0;
	}

	public boolean canVIEW_FULL_SHEET() {
		return hasPrivilege(Privileges.VIEW_FULL_SHEET);
	}

	public boolean canVIEW_OCCURRENCES() {
		return hasPrivilege(Privileges.VIEW_OCCURRENCES);
	}

	public boolean canMODIFY_TAXA_TERRITORIES() {
		return hasPrivilege(Privileges.MODIFY_TAXA_TERRITORIES);
	}

	public boolean canEDIT_FULL_CHECKLIST() {
		return hasPrivilege(Privileges.EDIT_FULL_CHECKLIST);
	}

	public boolean canEDIT_ANY_FIELD() {
		return hasAnyPrivilege(EDIT_ALL_FIELDS);
	}

	public boolean canEDIT_1_4() { return hasPrivilege(Privileges.EDIT_1_4); }

	public boolean canEDIT_SECTION2() {
		return hasPrivilege(EDIT_SECTION2);
	}

	public boolean canEDIT_SECTION3() {
		return hasPrivilege(EDIT_SECTION3);
	}

	public boolean canEDIT_SECTION4() {
		return hasPrivilege(EDIT_SECTION4);
	}

	public boolean canEDIT_SECTION5() {
		return hasPrivilege(EDIT_SECTION5);
	}

	public boolean canEDIT_SECTION6() {
		return hasPrivilege(Privileges.EDIT_SECTION6);
	}

	public boolean canEDIT_SECTION7() {
		return hasPrivilege(Privileges.EDIT_SECTION7);
	}

	public boolean canEDIT_SECTION8() {
		return hasPrivilege(Privileges.EDIT_SECTION8);
	}

	public boolean canEDIT_9_1_2_3_5() {
		return hasPrivilege(Privileges.EDIT_9_1_2_3_5);
	}

	public boolean canEDIT_9_4_9_7() {
		return hasPrivilege(Privileges.EDIT_9_4_9_7);
	}

	public boolean canEDIT_9_6_8_41() {
		return hasPrivilege(Privileges.EDIT_9_6_8_41);
	}

	public boolean canCREATE_REDLIST_DATASETS() {
		return hasPrivilege(Privileges.CREATE_REDLIST_DATASETS);
	}

	public boolean canEDIT_ALL_TEXTUAL() {
		return hasPrivilege(Privileges.EDIT_ALL_TEXTUAL);
	}

	public boolean canMANAGE_REDLIST_USERS() {
		return hasPrivilege(Privileges.MANAGE_REDLIST_USERS);
	}
}
