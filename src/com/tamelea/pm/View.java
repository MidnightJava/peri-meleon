package com.tamelea.pm;

import java.awt.event.ActionEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.tamelea.pm.data.ActiveHouseholdFilter;
import com.tamelea.pm.data.ActiveSelector;
import com.tamelea.pm.data.AddressField;
import com.tamelea.pm.data.AddressIndex;
import com.tamelea.pm.data.Data;
import com.tamelea.pm.data.HouseholdField;
import com.tamelea.pm.data.HouseholdFilter;
import com.tamelea.pm.data.HouseholdIndex;
import com.tamelea.pm.data.HouseholdNameComparator;
import com.tamelea.pm.data.MemberField;
import com.tamelea.pm.data.MemberIndex;
import com.tamelea.pm.data.PMString;
import com.tamelea.pm.data.PMTableModel;

@SuppressWarnings("serial")
public abstract class View extends JFrame {

	private final String MAP_SITE = "http://maps.google.com/maps";
	protected String country = "";
	protected PMTable table;//assigned by subclass
	protected Data data;//assigned by subclass
	public PMTableModel tableModel;//assigned by subclass

	public View(String title) {
		super(title);
	}

	public java.awt.Point getCenter() {
		java.awt.Dimension size = getSize();
		java.awt.Point loc = this.getLocation();
		return new java.awt.Point(loc.x + size.width / 2, loc.y + size.height / 2);
	}

	protected void displayMap(AddressIndex index, Data data){
		String sp = " ";
		String address = data.getAddressValue(index, AddressField.ADDRESS).toString();
		String address_2 = sp + data.getAddressValue(index, AddressField.ADDRESS_2).toString();
		String city = sp + data.getAddressValue(index, AddressField.CITY);
		String state = sp + data.getAddressValue(index, AddressField.STATE);
		String zip = sp + data.getAddressValue(index, AddressField.POSTAL_CODE);
		country = sp + data.getAddressValue(index, AddressField.COUNTRY);
		if ( ( address.equals("") && address_2.equals("") ) ||
				( !zipValid(zip.substring(1)) && ( city.equals("") || state.equals("") ) ) ){
			String msg = "Insufficient address information. Cannot display map.";
			notifyInsufficientAddress(msg);
			return;
		}
		if (zipValid(zip.substring(1))){
			city = "";
			state = "";
		}else{
			zip ="";
		}
		if (address_2.equals(sp)) address_2 = "";
		if (country.equals(sp)) country = "";
		String fullAddress = address + address_2 + city + state + zip  + country;
		//substitute pesky internal spaces
		String encodedAddress = "";
		try {
			encodedAddress = URLEncoder.encode(fullAddress, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String mapURL = MAP_SITE + "?q=" + encodedAddress;
		BrowserLaunch.openURL(mapURL);
	}

	protected void notifyInsufficientAddress(String msg){
		JOptionPane.showMessageDialog(this, msg);
	}

	//validate 5-digit or 9-digit (separated by dash) zip code
	private boolean zipValid(String zip){
		if (!country.substring(1).equals("")) return true;//assume foreign postal code
		int zipInt = 0;
		int[] zipPartLength = new int[]{5,4};
		String[] zipPart = zip.split("-");
		if (zipPart.length != 1 && zipPart.length != 2) return false;
		for (int i = 0; i < zipPart.length; i++){
			try{
				zipInt = Integer.parseInt(zipPart[i]);
			} catch (NumberFormatException e){
				return false;
			}
			if (zipInt <= 0) return false;
			if (zipPart[i].length() != zipPartLength[i]) return false;
		}
		return true;
	}

	public void showMap(){
		AddressIndex address;
		int tableIndex = table.getSelectedRow();
		if (tableIndex < 0) return;
		MemberIndex memberIndex = tableModel.getMemberIndex(tableIndex);
		AddressIndex tempAddress = 
				(AddressIndex)data.getMemberValue(memberIndex, MemberField.TEMP_ADDRESS);
		HouseholdIndex householdIndex = 
				(HouseholdIndex)data.getMemberValue(memberIndex, MemberField.HOUSEHOLD);
		AddressIndex householdAddress = (AddressIndex)data.getHouseholdValue(householdIndex, HouseholdField.ADDRESS);
		if (tempAddress == null){
			if (householdAddress == null){
				String msg = "There is no address record for the selected member";
				notifyInsufficientAddress(msg);
				return;
			}
			address = householdAddress;
		}else{
			address = tempAddress;
		}
		displayMap(address, data);
	}

	public final class HouseholdsExportAction extends AbstractAction {

		private ActiveSelector as;

		public HouseholdsExportAction(ActiveSelector as) {
			super(as == ActiveSelector.ACTIVE ? "Export active households info..." : "Export all households info...");
			this.as = as;
		}

		public void actionPerformed(ActionEvent e) {
			HouseholdFilter filter = null;
			// NB The enum ActiveSelector has a constant for non-active households for completeness only.
			// We filter only on active households or all households, not on non-active households
			if (as == ActiveSelector.ACTIVE) {
				filter = new ActiveHouseholdFilter(View.this.data);
			}
			HouseholdNameComparator sorter = new HouseholdNameComparator(View.this.data);
			List<HouseholdIndex> households = View.this.data.queryHouseholds(filter, sorter);
			for (HouseholdIndex household : households) {
				PMString name = (PMString) View.this.data.getHouseholdValue(household, HouseholdField.NAME);
				System.err.println(name);
				AddressIndex address = (AddressIndex) View.this.data.getHouseholdValue(household, HouseholdField.ADDRESS);
				PMString add1 = (PMString) View.this.data.getAddressValue(address, AddressField.ADDRESS);
				System.err.println(add1);
				PMString add2 = (PMString) View.this.data.getAddressValue(address, AddressField.ADDRESS);
				System.err.println(add2);
			}
		}
	}

	public abstract void editMember();

	public abstract void editHousehold();
}
