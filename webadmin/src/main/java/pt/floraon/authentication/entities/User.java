package pt.floraon.authentication.entities;

import com.google.gson.JsonObject;
import pt.floraon.driver.*;
import pt.floraon.driver.entities.NamedDBNode;
import pt.floraon.authentication.PasswordAuthentication;
import pt.floraon.occurrences.entities.Author;

import javax.xml.bind.annotation.XmlElementDecl;
import java.util.*;

import static pt.floraon.authentication.entities.User.Privileges.*;

/**
 * Represents a user of the Flora-On Server - not to be confounded with an {@link Author} of observations!
 */
public class User extends NamedDBNode {
	private String userName, password;
	private UserType userType;
	private Set<Privileges> privileges = new HashSet<>();
	private transient Set<Privileges> effectivePrivileges;
	/**
	 * The INodeKeys of the taxa to which the privileges apply. To all other taxa not listed here, only the
	 * VIEW_FULL_SHEET privilege applies. NOTE: If this set is empty, then the privileges apply to all taxa.
	 */
	private String[] applicableTaxa = new String[0];
	public enum UserType {ADMINISTRATOR, REGULAR}
	public enum PrivilegeType {CHECKLIST, REDLISTDATA, GLOBAL}
	public enum PrivilegeScope {PER_SPECIES, GLOBAL}

	public enum Privileges {
		VIEW_FULL_SHEET(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, VIEW_OCCURRENCES(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EXCLUDE_OCCURRENCES(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, MODIFY_OCCURRENCES(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, DOWNLOAD_OCCURRENCES(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_SECTION2(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_SECTION3(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_SECTION4(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_SECTION5(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_SECTION6(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_SECTION7(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_SECTION8(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_ALL_TEXTUAL(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_1_4(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_9_4_9_7(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_9_1_2_3_5(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_9_6_8_41(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_9_9_1(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_9_9_2(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_9_9_3(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, EDIT_9_9_4(PrivilegeType.REDLISTDATA, PrivilegeScope.PER_SPECIES)
		, CREATE_REDLIST_DATASETS(PrivilegeType.GLOBAL, PrivilegeScope.GLOBAL)
		, MODIFY_TAXA_TERRITORIES(PrivilegeType.CHECKLIST, PrivilegeScope.GLOBAL)
		, EDIT_FULL_CHECKLIST(PrivilegeType.CHECKLIST, PrivilegeScope.GLOBAL)
		, MANAGE_REDLIST_USERS(PrivilegeType.GLOBAL, PrivilegeScope.GLOBAL);

		private PrivilegeType privilegeType;
		private PrivilegeScope privilegeScope;

		Privileges(PrivilegeType pt, PrivilegeScope ps) {
			this.privilegeType = pt;
			this.privilegeScope = ps;
		}

		public PrivilegeType getPrivilegeType() {
			return this.privilegeType;
		}
		public PrivilegeScope getPrivilegeScope() {return this.privilegeScope;}
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
			, EDIT_9_1_2_3_5, EDIT_9_6_8_41, EDIT_9_9_1, EDIT_9_9_2, EDIT_9_9_3, EDIT_9_9_4 };

	public static Privileges[] DEFAULT_USER_PRIVILEGES = new Privileges[] { VIEW_FULL_SHEET };

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
		resetEffectivePrivileges();
	}

	public User(String username, String fullName, Set<Privileges> privileges) throws DatabaseException {
		super(fullName);
		this.userName = username;
		this.privileges = privileges;
		this.userType = UserType.REGULAR;
		resetEffectivePrivileges();
	}

	public User(String username, String fullName, Privileges[] privileges) throws DatabaseException {
		this(username, fullName, new HashSet<>(Arrays.asList(privileges)));
	}

	public static User guest() throws DatabaseException {
		User u = new User("guest", "Guest", new Privileges[] {});
		return u;
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
		if(value) {
			this.privileges.add(privilege);
			this.effectivePrivileges.add(privilege);
		} else {
			this.privileges.remove(privilege);
			this.effectivePrivileges.remove(privilege);
		}
	}

	public boolean isGuest() {
		return privileges.size() == 0;
	}

	public void setPrivileges(Privileges[] assignedPrivileges) {
		this.privileges.addAll(Arrays.asList(assignedPrivileges));
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

	public void setEDIT_9_9_1(boolean value) {
		setPrivilege(EDIT_9_9_1, value);
	}

	public void setEDIT_9_9_2(boolean value) {
		setPrivilege(EDIT_9_9_2, value);
	}

	public void setEDIT_9_9_3(boolean value) {
		setPrivilege(EDIT_9_9_3, value);
	}

	public void setEDIT_9_9_4(boolean value) {
		setPrivilege(EDIT_9_9_4, value);
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

	/**
	 * Tests whether the user has given privilege for the current taxon. This should only be used after calling
	 * {@link User#setEffectivePrivilegesFor(IFloraOn, INodeKey)}
	 * @param privilege
	 * @return
	 */
	public boolean hasPrivilege(Privileges privilege) {
		return this.effectivePrivileges.contains(privilege);
	}

	/**
	 * Checks whether the user has this privilege assigned. This does not take into account the current taxon.
	 * @param privilege
	 * @return
	 */
	public boolean hasAssignedPrivilege(Privileges privilege) {
		return this.privileges.contains(privilege);
	}

	public boolean hasAnyPrivilege(Privileges[] privileges) {
		Set<Privileges> tmp = new HashSet<>(Arrays.asList(privileges));
		tmp.retainAll(this.effectivePrivileges);
		return tmp.size() > 0;
	}

	public boolean canVIEW_FULL_SHEET() {
		return hasPrivilege(Privileges.VIEW_FULL_SHEET);
	}

	public boolean canVIEW_OCCURRENCES() { return hasPrivilege(Privileges.VIEW_OCCURRENCES); }

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

	public boolean canEDIT_9_9_1() {
		return hasPrivilege(Privileges.EDIT_9_9_1);
	}

	public boolean canEDIT_9_9_2() {
		return hasPrivilege(Privileges.EDIT_9_9_2);
	}

	public boolean canEDIT_9_9_3() {
		return hasPrivilege(Privileges.EDIT_9_9_3);
	}

	public boolean canEDIT_9_9_4() {
		return hasPrivilege(Privileges.EDIT_9_9_4);
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

	public String[] getApplicableTaxa() {
		return applicableTaxa;
	}

	public void setApplicableTaxa(String[] applicableTaxa) {
		this.applicableTaxa = applicableTaxa;
	}

	/**
	 * Sets the effective privileges for the given taxon
	 * @param driver
	 * @param taxonID
	 */
	public void setEffectivePrivilegesFor(IFloraOn driver, INodeKey taxonID) throws FloraOnException {
		if(this.applicableTaxa.length == 0) {	// privileges apply for all taxa
			this.effectivePrivileges = this.privileges;
			return;
		}

		if(taxonID != null) {
			for (String taxon : this.applicableTaxa) {
				if (driver.wrapTaxEnt(taxonID).isInfrataxonOf(driver.asNodeKey(taxon))) {    // is the taxon covered by the privileges?
					this.effectivePrivileges = this.privileges;
					return;
				}
			}
		}

		this.effectivePrivileges = new HashSet<>(Arrays.asList(User.DEFAULT_USER_PRIVILEGES));
		for(Privileges p : this.privileges) {
			if(p.getPrivilegeScope() == PrivilegeScope.GLOBAL)
				this.effectivePrivileges.add(p);
		}

	}

	/**
	 * Sets the effective privileges for only those that are not taxon based.
	 */
	public void resetEffectivePrivileges() {
		try {
			setEffectivePrivilegesFor(null, null);
		} catch (FloraOnException e) {
			// it'll never be thrown if driver == null
			e.printStackTrace();
		}
	}

	/**
	 * Temporally remove given privileges for this user
	 * @param privileges
	 */
	public void revokePrivileges(Privileges[] privileges) {
		this.effectivePrivileges = new HashSet<>(Arrays.asList(User.DEFAULT_USER_PRIVILEGES));
		this.effectivePrivileges.addAll(this.privileges);
		this.effectivePrivileges.removeAll(new HashSet<>(Arrays.asList(privileges)));
	}
}
