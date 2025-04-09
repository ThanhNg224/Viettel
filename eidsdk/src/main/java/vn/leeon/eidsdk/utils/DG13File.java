package vn.leeon.eidsdk.utils;

import android.util.Log;

import net.sf.scuba.tlv.TLVInputStream;

import org.jmrtd.lds.DataGroup;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import kotlin.text.Charsets;

/**
 * File structure for the EF_DG13 file.
 * Datagroup 13 contains optional personal detail(s).
 * <p>
 * All fields are optional. See Section 16 of LDS-TR.
 * <ol>
 * <li>Electronic Identification Number (EID)</li>
 * <li>Name of Holder (Primary and Secondary Identifiers, in full)</li>
 * <li>Date of Birth (in full)</li>
 * <li>Gender</li>
 * <li>Nationality</li>
 * <li>Ethnicity</li>
 * <li>Religion</li>
 * <li>Place of Origin</li>
 * <li>Place of Residence</li>
 * <li>Personal Identification</li>
 * <li>Date Of Issue</li>
 * <li>Date Of Expiry</li>
 * <li>Father Name</li>
 * <li>Mother Name</li>
 * <li>Old EID Number</li>
 * <li>UNK ID Number</li>
 * <li>UNK Info</li>
 * </ol>
 *
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1869 $
 */
public class DG13File extends DataGroup {

    private static final Logger LOGGER = Logger.getLogger("org.jmrtd");

    private static final char IDX_EID = 1;
    public static final char[] PREFIX_EID = new char[]{48, 17, 2, 1, 1, 19, 12};

    public static final char IDX_FULLNAME = 2;
    public static final char[] PREFIX_FULLNAME = new char[]{48, 28, 2, 1, 2, 12, 23};

    public static final char IDX_DOB = 3;
    public static final char[] PREFIX_DOB = new char[]{48, 15, 2, 1, 3, 19, 10};

    public static final char IDX_GENDER = 4;
    public static final char[] PREFIX_GENDER = new char[]{48, 8, 2, 1, 4, 12, 3};

    public static final char IDX_NATIONALITY = 5;
    public static final char[] PREFIX_NATIONALITY = new char[]{48, 15, 2, 1, 5, 12, 10};

    public static final char IDX_ETHNICITY = 6;
    public static final char[] PREFIX_ETHNICITY = new char[]{48, 9, 2, 1, 6, 12, 4};

    public static final char IDX_RELIGION = 7;
    public static final char[] PREFIX_RELIGION = new char[]{48, 11, 2, 1, 7, 12, 6};

    public static final char IDX_POG = 8;
    public static final char[] PREFIX_POG = new char[]{48, 38, 2, 1, 8, 12, 33};

    public static final char IDX_POR = 9;
    public static final char[] PREFIX_POR = new char[]{48, 61, 2, 1, 9, 12, 56};

    public static final char IDX_PERSONAL_IDENTIFICATION = 10;
    public static final char[] PREFIX_PERSONAL_IDENTIFICATION = new char[]{48, 40, 2, 1, 10, 12, 35};

    public static final char IDX_DATEOFISSUE = 11;
    public static final char[] PREFIX_DATEOFISSUE = new char[]{48, 15, 2, 1, 11, 19, 10};

    public static final char IDX_DATEOFEXPIRY = 12;
    public static final char[] PREFIX_DATEOFEXPIRY = new char[]{48, 15, 2, 1, 12, 12, 10};

    public static final char IDX_FAMILY = 13;
    public static final char[] PREFIX_FAMILY = new char[]{48, 54, 2, 1, 13};
    public static final char[] PREFIX_FATHERNAME = new char[]{48, 25, 12, 23};
    public static final char[] PREFIX_MOTHERNAME = new char[]{48, 22, 12, 20};
    public static final char[] PREFIX_OTHERNAME = new char[]{'0', 20, 2, 1, 14, '0', 15, '\f', '\r'};
    public static final char IDX_CARDINFO = 14;
    public static final char[] PREFIX_CARDINFO = new char[]{48, 3, 2, 1, 14};
    public static final char IDX_OLDEID = 15;
    public static final char[] PREFIX_OLDEID = new char[]{48, 14, 2, 1, 15, 19, 9};
    public static final char IDX_CARDUNK = 16;
    public static final char[] PREFIX_UNK = new char[]{48, 21, 2, 1, 16, 19, 16};

    private String eidNumber;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String nationality;

    /**
     * Returns the EID number of the holder
     *
     * @return EID number
     */
    public String getEidNumber() {
        return eidNumber;
    }

    /**
     * Returns full name of the holder (primary and secondary identifiers).
     *
     * @return the name of the holder
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the date of birth of the holder
     *
     * @return the date of birth of the holder
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Returns the gender of the holder
     *
     * @return the gender of the holder
     */
    public String getGender() {
        return gender;
    }

    /**
     * Returns nationality of the holder
     *
     * @return nationality of the holder
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * Returns the ethnicity of the holder
     *
     * @return the ethnicity of the holder
     */
    public String getEthnicity() {
        return ethnicity;
    }

    /**
     * Returns the religion of the holder
     *
     * @return the religion of the holder
     */
    public String getReligion() {
        return religion;
    }

    /**
     * Returns place of origin of the holder
     *
     * @return place of origin of the holder
     */
    public String getPlaceOfOrigin() {
        return placeOfOrigin;
    }

    /**
     * Returns place of residence of the holder
     *
     * @return place of residence of the holder
     */
    public String getPlaceOfResidence() {
        return placeOfResidence;
    }

    /**
     * Returns personal identification of the holder
     *
     * @return personal identification of the holder
     */
    public String getPersonalIdentification() {
        return personalIdentification;
    }

    /**
     * Returns date of issue of the card
     *
     * @return date of issue of the card
     */
    public String getDateOfIssue() {
        return dateOfIssue;
    }

    /**
     * Returns date of expiry of the card
     *
     * @return date of expiry of the card
     */
    public String getDateOfExpiry() {
        return dateOfExpiry;
    }

    /**
     * Returns the father name of the holder
     *
     * @return the father name of the holder
     */
    public String getFatherName() {
        return fatherName;
    }

    /**
     * Returns the mother name of the holder
     *
     * @return the mother name of the holder
     */
    public String getMotherName() {
        return motherName;
    }

    public String getWifeOrHusbandName() {
        return WifeOrHusbandName;
    }

    /**
     * Returns the old EID Number of the holder
     *
     * @return the old EID Number of the holder
     */
    public String getOldEidNumber() {
        return oldEidNumber;
    }

    /**
     * Returns the UNK ID Number of the holder
     *
     * @return the UNK ID Number of the holder
     */
    public String getUnkIdNumber() {
        return unkIdNumber;
    }

    /**
     * Returns the UNK Info
     *
     * @return the UNK Info
     */
    public List<String> getUnkInfo() {
        return unkInfo;
    }

    private String ethnicity;
    private String religion;
    private String placeOfOrigin;
    private String placeOfResidence;
    private String personalIdentification;
    private String dateOfIssue;
    private String dateOfExpiry;
    private String fatherName;
    private String motherName;
    private String WifeOrHusbandName;
    private String oldEidNumber;
    private String unkIdNumber;
    private List<String> unkInfo = new ArrayList<>();

    public DG13File(InputStream inputStream) throws IOException {
        super(EF_DG13_TAG, inputStream);
    }

    /**
     * data segment always start with 48, _, 2, 1, {segmentIdx}
     * 1 <= segmentIdx <= 14
     * see PREFIX_ for more info
     *
     * @param inputStream the input stream to read from
     */
    @Override
    protected void readContent(InputStream inputStream) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(inputStream instanceof TLVInputStream ? inputStream : new TLVInputStream(inputStream)), Charsets.UTF_8));
            char[] buf = new char[2048];
            int numRead = bufferedReader.read(buf);
            LOGGER.log(Level.INFO, "numRead", numRead);
            List<Integer> separatorPositions = new ArrayList<>();
            int segmentIdx = 1;
            for (int i = 0; i < buf.length - 5; ++i) {
                char[] c5 = new char[]{buf[i], buf[i + 1], buf[i + 2], buf[i + 3], buf[i + 4]};
                if (c5[0] == 48 && c5[2] == 2 && c5[3] == 1 && c5[4] == segmentIdx) {
                    ++segmentIdx; // increment next segment
                    separatorPositions.add(i);
                } else if (c5[0] == 0 && c5[1] == 0 && c5[2] == 0 && c5[3] == 0) {
                    separatorPositions.add(i); // end of data
                    break;
                }
            }
            for (int i = 0; i < separatorPositions.size() - 1; ++i) {
                int start = separatorPositions.get(i);
                int end = separatorPositions.get(i + 1);

                char[] subset = Arrays.copyOfRange(buf, start, end);
                // Potential empty group here
                if (subset.length < 5) {
                    continue;
                }

                switch (subset[4]) {
                    case IDX_EID:
                        this.eidNumber = new String(Arrays.copyOfRange(subset, PREFIX_EID.length, subset.length));
                        break;
                    case IDX_FULLNAME:
                        this.fullName = new String(Arrays.copyOfRange(subset, PREFIX_FULLNAME.length, subset.length));
                        break;
                    case IDX_DOB:
                        this.dateOfBirth = new String(Arrays.copyOfRange(subset, PREFIX_DOB.length, subset.length));
                        break;
                    case IDX_GENDER:
                        this.gender = new String(Arrays.copyOfRange(subset, PREFIX_GENDER.length, subset.length));
                        break;
                    case IDX_NATIONALITY:
                        this.nationality = new String(Arrays.copyOfRange(subset, PREFIX_NATIONALITY.length, subset.length));
                        break;
                    case IDX_ETHNICITY:
                        this.ethnicity = new String(Arrays.copyOfRange(subset, PREFIX_ETHNICITY.length, subset.length));
                        break;
                    case IDX_RELIGION:
                        this.religion = new String(Arrays.copyOfRange(subset, PREFIX_RELIGION.length, subset.length));
                        break;
                    case IDX_POG:
                        this.placeOfOrigin = new String(Arrays.copyOfRange(subset, PREFIX_POG.length, subset.length));
                        break;
                    case IDX_POR:
                        this.placeOfResidence = new String(Arrays.copyOfRange(subset, PREFIX_POR.length, subset.length));
                        break;
                    case IDX_PERSONAL_IDENTIFICATION:
                        this.personalIdentification = new String(Arrays.copyOfRange(subset, PREFIX_PERSONAL_IDENTIFICATION.length, subset.length));
                        break;
                    case IDX_DATEOFISSUE:
                        this.dateOfIssue = new String(Arrays.copyOfRange(subset, PREFIX_DATEOFISSUE.length, subset.length));
                        break;
                    case IDX_DATEOFEXPIRY:
                        this.dateOfExpiry = new String(Arrays.copyOfRange(subset, PREFIX_DATEOFEXPIRY.length, subset.length));
                        break;
                    case IDX_FAMILY:
                        List<Integer> seps = new ArrayList<>();
                        for (int j = PREFIX_FAMILY.length; j < subset.length - 2; ++j) {
                            if (subset[j] == 48 && subset[j + 2] == 12) {
                                seps.add(j);
                            }
                        }
                        if (seps.size() != 2) {
                            Log.e("FAMILY", "Bad format");
                            break;
                        }
                        this.fatherName = new String(Arrays.copyOfRange(subset, seps.get(0) + PREFIX_FATHERNAME.length, seps.get(1)));
                        this.motherName = new String(Arrays.copyOfRange(subset, seps.get(1) + PREFIX_MOTHERNAME.length, subset.length));
                        break;
                    case IDX_CARDINFO:
                        this.WifeOrHusbandName = new String(Arrays.copyOfRange(subset, PREFIX_OTHERNAME.length, subset.length));
//                    empty data
//                    data.setExpiryDate(new String(Arrays.copyOfRange(subset, PREFIX_EXPIRYDATE.length, subset.length)));
                        break;
                    case IDX_OLDEID:
                        this.oldEidNumber = new String(Arrays.copyOfRange(subset, PREFIX_OLDEID.length, subset.length));
                        break;
                    case IDX_CARDUNK:
                        this.unkIdNumber = new String(Arrays.copyOfRange(subset, PREFIX_UNK.length, subset.length));
                        break;
                    default:
                        this.unkInfo.add(new String(subset));
                        break;
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Exception", e);
        }
    }

    @Override
    protected void writeContent(OutputStream outputStream) throws IOException {
        // NO IMPLEMENTATION
    }
}