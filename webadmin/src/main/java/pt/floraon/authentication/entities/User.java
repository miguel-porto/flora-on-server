package pt.floraon.authentication.entities;

import com.arangodb.velocypack.annotations.Expose;
import com.google.gson.JsonObject;
import pt.floraon.authentication.Privileges;
import pt.floraon.driver.*;
import pt.floraon.driver.entities.NamedDBNode;
import pt.floraon.authentication.PasswordAuthentication;
import pt.floraon.driver.interfaces.IFloraOn;
import pt.floraon.driver.interfaces.INodeKey;
import pt.floraon.geometry.PolygonTheme;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static pt.floraon.authentication.Privileges.*;

/**
 * Represents a user of the Flora-On Server, who can be an observer, collector, etc.
 */
public class User extends NamedDBNode {
	private String userName, password;
	private UserType userType;
	private Set<Privileges> privileges = new HashSet<>();
	private List<TaxonPrivileges> taxonPrivileges;
	private List<String> uploadedTables;
	private String userPolygons;
	public enum UserType {ADMINISTRATOR, REGULAR}

	@Expose(serialize = false, deserialize = false)
	private transient Set<Privileges> effectivePrivileges;

	@Expose(serialize = false, deserialize = false)
	private transient PolygonTheme userPolygonsTheme;

	public static Map<String, Privileges[]> userProfiles;
	static {
		userProfiles = new HashMap<>();
		userProfiles.put("AUTHOR", new Privileges[] { EDIT_1_4, EDIT_SECTION2, EDIT_SECTION3, EDIT_SECTION4
				, EDIT_SECTION5, EDIT_SECTION6, EDIT_SECTION7, EDIT_SECTION8, EDIT_9_7_9_92});
		userProfiles.put("EVALUATOR", new Privileges[] {EDIT_9_1_2_3_4});
		userProfiles.put("REVIEWER", new Privileges[] {EDIT_9_3_9_45, EDIT_9_1_2_3_4});
	}

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

	public boolean isAdministrator() {
		return userType != null && userType == UserType.ADMINISTRATOR;
	}

	public void setUserType(String userType) {
		this.userType = UserType.valueOf(userType);
	}

	public String getUserPolygons() {
		return userPolygons;
	}

	public PolygonTheme _getUserPolygonsAsTheme() {
		if(userPolygons == null) return null;
		if(userPolygonsTheme == null) {
			InputStream stream = new ByteArrayInputStream(userPolygons.getBytes(StandardCharsets.UTF_8));
			return userPolygonsTheme = new PolygonTheme(stream, null);
		} else return userPolygonsTheme;
	}

	public void setUserPolygons(String userPolygons) {
		this.userPolygons = userPolygons;
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

	public List<String> getUploadedTables() {
		return uploadedTables == null ? Collections.<String>emptyList() : uploadedTables;
	}

	public void setUploadedTables(List<String> uploadedTables) {
		this.uploadedTables = uploadedTables;
	}

	public void addUploadedTable(String uploadedTable) {
		if(uploadedTables == null)
			uploadedTables = new ArrayList<>();
		uploadedTables.add(uploadedTable);
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

	public void setEXCLUDE_OCCURRENCES(boolean value) {
		setPrivilege(EXCLUDE_OCCURRENCES, value);
	}

	public void setMODIFY_OCCURRENCES(boolean value) {
		setPrivilege(MODIFY_OCCURRENCES, value);
	}

	public void setDOWNLOAD_OCCURRENCES(boolean value) {
		setPrivilege(DOWNLOAD_OCCURRENCES, value);
	}

	public void setEDIT_SECTION2(boolean value) {
		setPrivilege(EDIT_SECTION2, value);
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

	public void setEDIT_SECTION9(boolean value) {
		setPrivilege(EDIT_SECTION9, value);
	}

	public void setEDIT_ALL_TEXTUAL(boolean value) {
		setPrivilege(EDIT_ALL_TEXTUAL, value);
	}

	public void setEDIT_ALL_1_8(boolean value) {
		setPrivilege(EDIT_ALL_1_8, value);
	}

	public void setEDIT_1_4(boolean value) {
		setPrivilege(EDIT_1_4, value);
	}

	public void setEDIT_7_3(boolean value) {
		setPrivilege(EDIT_7_3, value);
	}

	public void setEDIT_4_2(boolean value) {
		setPrivilege(EDIT_4_2, value);
	}

	public void setEDIT_6_2(boolean value) {
		setPrivilege(EDIT_6_2, value);
	}

	public void setEDIT_9_7_9_92(boolean value) {
		setPrivilege(EDIT_9_7_9_92, value);
	}

	public void setEDIT_9_1_2_3_4(boolean value) {
		setPrivilege(EDIT_9_1_2_3_4, value);
	}

	public void setEDIT_9_3_9_45(boolean value) {
		setPrivilege(EDIT_9_3_9_45, value);
	}

	public void setEDIT_9_5_9_6_9_61_9_91(boolean value) {
		setPrivilege(EDIT_9_5_9_6_9_61_9_91, value);
	}

	public void setEDIT_9_8_9_93(boolean value) {
		setPrivilege(EDIT_9_8_9_93, value);
	}

	public void setEDIT_9_9_4(boolean value) {
		setPrivilege(EDIT_9_9_4, value);
	}

	public void setCREATE_REDLIST_DATASETS(boolean value) {
		setPrivilege(CREATE_REDLIST_DATASETS, value);
	}

	public void setMODIFY_TAXA(boolean value) {
		setPrivilege(MODIFY_TAXA, value);
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

	public boolean canDOWNLOAD_OCCURRENCES() { return hasPrivilege(Privileges.DOWNLOAD_OCCURRENCES); }

	public boolean canMODIFY_TAXA() {
		return hasPrivilege(Privileges.MODIFY_TAXA);
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

	public boolean canEDIT_1_4() { return hasPrivilege(Privileges.EDIT_1_4) || hasPrivilege(EDIT_ALL_1_8); }

	public boolean canEDIT_7_3() { return hasPrivilege(Privileges.EDIT_7_3) || hasPrivilege(EDIT_ALL_1_8); }

	public boolean canEDIT_4_2() { return hasPrivilege(Privileges.EDIT_4_2) || hasPrivilege(EDIT_ALL_1_8); }

	public boolean canEDIT_6_2() { return hasPrivilege(Privileges.EDIT_6_2) || hasPrivilege(EDIT_ALL_1_8); }

	public boolean canEDIT_SECTION2() {
		return hasPrivilege(EDIT_SECTION2) || hasPrivilege(EDIT_ALL_1_8);
	}

	public boolean canEDIT_SECTION3() {
		return hasPrivilege(EDIT_SECTION3) || hasPrivilege(EDIT_ALL_1_8);
	}

	public boolean canEDIT_SECTION4() {
		return hasPrivilege(EDIT_SECTION4) || hasPrivilege(EDIT_ALL_1_8);
	}

	public boolean canEDIT_SECTION5() {
		return hasPrivilege(EDIT_SECTION5) || hasPrivilege(EDIT_ALL_1_8);
	}

	public boolean canEDIT_SECTION6() {
		return hasPrivilege(Privileges.EDIT_SECTION6) || hasPrivilege(EDIT_ALL_1_8);
	}

	public boolean canEDIT_SECTION7() {
		return hasPrivilege(Privileges.EDIT_SECTION7) || hasPrivilege(EDIT_ALL_1_8);
	}

	public boolean canEDIT_SECTION8() {
		return hasPrivilege(Privileges.EDIT_SECTION8) || hasPrivilege(EDIT_ALL_1_8);
	}

	public boolean canEDIT_9_1_2_3_4() {
		return hasPrivilege(Privileges.EDIT_9_1_2_3_4) || hasPrivilege(EDIT_SECTION9);
	}

	public boolean canEDIT_9_7_9_92() {
		return hasPrivilege(Privileges.EDIT_9_7_9_92) || hasPrivilege(EDIT_SECTION9);
	}

	public boolean canEDIT_9_3_9_45() {
		return hasPrivilege(Privileges.EDIT_9_3_9_45) || hasPrivilege(EDIT_SECTION9);
	}

	public boolean canEDIT_9_5_9_6_9_61_9_91() {
		return hasPrivilege(Privileges.EDIT_9_5_9_6_9_61_9_91) || hasPrivilege(EDIT_SECTION9);
	}

	public boolean canEDIT_9_8_9_93() {
		return hasPrivilege(Privileges.EDIT_9_8_9_93) || hasPrivilege(EDIT_SECTION9);
	}

	public boolean canEDIT_9_9_4() {
		return hasPrivilege(Privileges.EDIT_9_9_4) || hasPrivilege(EDIT_SECTION9);
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

	public boolean canMODIFY_OCCURRENCES()  {
		return hasPrivilege(Privileges.MODIFY_OCCURRENCES);
	}

	public void addTaxonPrivileges(String[] taxa, String[] privileges) {
		if(this.taxonPrivileges == null)
			this.taxonPrivileges = new ArrayList<>();
		this.taxonPrivileges.add(new TaxonPrivileges(taxa, privileges));
	}

	public void addTaxonPrivileges(String[] taxa, Set<Privileges> privileges) {
		if(this.taxonPrivileges == null)
			this.taxonPrivileges = new ArrayList<>();
		this.taxonPrivileges.add(new TaxonPrivileges(taxa, privileges));
	}

	public List<TaxonPrivileges> getTaxonPrivileges() {
		return taxonPrivileges == null ? Collections.<TaxonPrivileges>emptyList() : taxonPrivileges;
	}

	public void setTaxonPrivileges(List<TaxonPrivileges> taxonPrivileges) {
		this.taxonPrivileges = taxonPrivileges;
	}

	/**
	 * Sets the effective privileges for the given taxon
	 * @param driver
	 * @param taxonID
	 */
	public void setEffectivePrivilegesFor(IFloraOn driver, INodeKey taxonID) throws FloraOnException {
		if(this.taxonPrivileges == null || this.taxonPrivileges.size() == 0) {	// privileges are the same as for all taxa
			this.effectivePrivileges = this.privileges;
			return;
		}
		this.effectivePrivileges = new HashSet<>();
		if(taxonID != null) {
			for (TaxonPrivileges taxon : this.taxonPrivileges) {
				this.effectivePrivileges.addAll(taxon.getPrivilegesForTaxon(driver, taxonID));
			}
		}

//		this.effectivePrivileges = new HashSet<>(Arrays.asList(User.DEFAULT_USER_PRIVILEGES));

		// add global privileges (those that are not taxon-wise by construction)
		for(Privileges p : this.privileges) {
//			if(p.getPrivilegeScope() == PrivilegeScope.GLOBAL)
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
