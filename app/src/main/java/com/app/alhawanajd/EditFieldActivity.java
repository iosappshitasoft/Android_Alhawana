package com.app.alhawanajd;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.app.alhawanajd.databinding.ActivityEditFieldBinding;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditFieldActivity extends BaseFragmentActivity {

    public static final String FIELD_TYPE = "field_type";
    public static final String FIELD_VALUE = "field_value";
    public static final int EDIT_FIELD_REQUEST_CODE = 2000;

    private ActivityEditFieldBinding binding;
    private String currentFieldType;
    private ApiInterface apiInterface;
    private Calendar myCalendar;
    String linkFrom = "";
    String TAG = "EditFieldActivity";

    private boolean isKeyboardVisible = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditFieldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        myCalendar = Calendar.getInstance();

        getIntentData();
        initListeners();

        if (LocaleManager.isRTL()) {
            binding.backArrow.setRotation(180);
           // binding.instagramlinkArrow.setRotation(180);
           // binding.youtubelinkArrow.setRotation(180);
        } else {
            binding.backArrow.setRotation(0);
           // binding.instagramlinkArrow.setRotation(0);
           // binding.youtubelinkArrow.setRotation(0);
        }
        setupKeyboardListener();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            currentFieldType = intent.getStringExtra(FIELD_TYPE);
            String fieldValue = intent.getStringExtra(FIELD_VALUE);
            setupUIForField(currentFieldType, fieldValue);
        }
    }

    private void setupUIForField(String fieldType, String fieldValue) {
        // Hide all sections first
        binding.nameSection.setVisibility(View.GONE);
        binding.usernameSection.setVisibility(View.GONE);
        binding.bioSection.setVisibility(View.GONE);
        binding.mobileNumberSection.setVisibility(View.GONE);
        binding.genderSection.setVisibility(View.GONE);
        binding.dobSection.setVisibility(View.GONE);
        binding.paypalIdSection.setVisibility(View.GONE);
        binding.linkSection.setVisibility(View.GONE);

       // binding.topBar.txtTitle.setText(fieldTitle(fieldType));

        switch (fieldType) {
            case Constants.TAG_NAME:
                binding.nameSection.setVisibility(View.VISIBLE);
                binding.nameEditText.setText(fieldValue);
                binding.nameEditText.setHint("Enter your name");
                binding.nameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                binding.nameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_LENGTH)});
                updateNameCharCount(fieldValue != null ? fieldValue.length() : 0);
                binding.nameEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        updateNameCharCount(s.length());
                        binding.clearNameTextButton.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() > Constants.MAX_LENGTH) {
                            Toast.makeText(EditFieldActivity.this, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                binding.clearNameTextButton.setVisibility(TextUtils.isEmpty(fieldValue) ? View.GONE : View.VISIBLE);
                break;

            case Constants.TAG_USER_NAME:
                binding.usernameSection.setVisibility(View.VISIBLE);
                binding.usernameEditText.setText(fieldValue);
                binding.usernameEditText.setHint(getString(R.string.username));
                binding.profileLinkText.setText("https://console.alhawanajd.com/"+fieldValue);
                binding.usernameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                binding.usernameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.USERNAME_MAX_LENGTH)});
                updateUsernameCharCount(fieldValue != null ? fieldValue.length() : 0);
                binding.profileLinkText.setVisibility(View.VISIBLE);
                binding.usernameEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        updateUsernameCharCount(s.length());
                       // validateUsername(s.toString());
                        binding.clearUsernameTextButton.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() > Constants.USERNAME_MAX_LENGTH) {
                            Toast.makeText(EditFieldActivity.this, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
               // validateUsername(fieldValue);
                binding.clearUsernameTextButton.setVisibility(TextUtils.isEmpty(fieldValue) ? View.GONE : View.VISIBLE);
                break;

            case Constants.TAG_BIO:
                binding.bioSection.setVisibility(View.VISIBLE);
                binding.bioEditText.setText(fieldValue);
                binding.bioEditText.setHint("Write a short description about who you are or what your account is about");
                binding.bioEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                binding.bioEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.BIO_MAX_LENGTH)});
                updateBioCharCount(fieldValue != null ? fieldValue.length() : 0);
                binding.mentionButton.setVisibility(View.VISIBLE);
                binding.bioEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        updateBioCharCount(s.length());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() > Constants.BIO_MAX_LENGTH) {
                            Toast.makeText(EditFieldActivity.this, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;

            case Constants.TAG_MOBILE_NUMBER:
                binding.mobileNumberSection.setVisibility(View.VISIBLE);
                binding.mobileNumberEditText.setText(fieldValue);
                binding.mobileNumberEditText.setHint("Enter your mobile number");
                binding.mobileNumberEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                binding.mobileNumberEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
                binding.mobileNumberEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        binding.clearMobileNumberTextButton.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length() > 10) {
                            Toast.makeText(EditFieldActivity.this, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                binding.clearMobileNumberTextButton.setVisibility(TextUtils.isEmpty(fieldValue) ? View.GONE : View.VISIBLE);
                break;

            case Constants.TAG_GENDER:
                binding.genderSection.setVisibility(View.VISIBLE);
                binding.genderEditText.setText(fieldValue);
                binding.genderEditText.setHint("Enter your gender");
                binding.genderEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                binding.genderEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        binding.clearGenderTextButton.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                binding.clearGenderTextButton.setVisibility(TextUtils.isEmpty(fieldValue) ? View.GONE : View.VISIBLE);
                break;

            case Constants.TAG_DOB:
                binding.dobSection.setVisibility(View.VISIBLE);
                binding.dobEditText.setText(fieldValue);
                binding.dobEditText.setHint("Select your date of birth");
                binding.dobEditText.setFocusable(false);
                binding.dobEditText.setClickable(true);
                binding.dobEditText.setInputType(InputType.TYPE_NULL);
                if (fieldValue != null && !fieldValue.isEmpty() && !fieldValue.equals("DOB")) {
                    updateCalendarFromText(fieldValue);
                }
                binding.clearDobTextButton.setVisibility(TextUtils.isEmpty(fieldValue) || fieldValue.equals("DOB") ? View.GONE : View.VISIBLE);
                break;

            case Constants.TAG_PAYPAL_ID:
                binding.paypalIdSection.setVisibility(View.VISIBLE);
                binding.paypalIdEditText.setText(fieldValue);
                binding.paypalIdEditText.setHint("Enter your PayPal ID");
                binding.paypalIdEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                binding.paypalIdEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        binding.clearPaypalIdTextButton.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                binding.clearPaypalIdTextButton.setVisibility(TextUtils.isEmpty(fieldValue) ? View.GONE : View.VISIBLE);
                break;

            case Constants.TAG_LINK:
                binding.linkSection.setVisibility(View.VISIBLE);
                binding.topBar.setVisibility(View.GONE);
                binding.topBarLink.setVisibility(View.VISIBLE);
                String instagramLink = GetSet.getInstagram_link();
                String youtubeLink = GetSet.getYoutube_link();

                if (TextUtils.isEmpty(instagramLink)) {
                    binding.instagramlinkValue.setText(getString(R.string.enter_addlink));
                } else {
                    binding.instagramlinkValue.setText(instagramLink);
                }

                if (TextUtils.isEmpty(youtubeLink)) {
                    binding.youtubelinkValue.setText(getString(R.string.enter_addlink));
                } else {
                    binding.youtubelinkValue.setText(youtubeLink);
                }
                break;
        }
    }



    private void setupKeyboardListener() {
        final View rootView = binding.getRoot();

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);

            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            boolean keyboardNowVisible = keypadHeight > screenHeight * 0.15;

            if (keyboardNowVisible != isKeyboardVisible) {
                isKeyboardVisible = keyboardNowVisible;

                if (isKeyboardVisible) {
                    binding.mentionButton.setVisibility(View.VISIBLE);
                } else {
                    binding.mentionButton.setVisibility(View.GONE);
                }
            }
        });
    }


    private String fieldTitle(String fieldType) {
        switch (fieldType) {
            case Constants.TAG_NAME: return "Name";
            case Constants.TAG_USER_NAME: return "Username";
            case Constants.TAG_BIO: return "Bio";
            case Constants.TAG_MOBILE_NUMBER: return "Mobile Number";
            case Constants.TAG_GENDER: return "Gender";
            case Constants.TAG_DOB: return "Date of Birth";
            case Constants.TAG_PAYPAL_ID: return "PayPal ID";
            case Constants.TAG_LINK: return "Link";
            default: return "Edit Field";
        }
    }

    private void initListeners() {
        binding.topBar.findViewById(R.id.cancelButton).setOnClickListener(v -> {
            if(!TextUtils.isEmpty(linkFrom)){
                binding.datalinkSection.setVisibility(View.GONE);
                binding.instaMainLay.setVisibility(View.VISIBLE);
                binding.youtubeMainLay.setVisibility(View.VISIBLE);
                linkFrom = "";
                binding.topBar.setVisibility(View.GONE);
                binding.topBarLink.setVisibility(View.VISIBLE);
            }else {
                setResult(RESULT_CANCELED);
                finish();
            }
         });

        binding.topBar.findViewById(R.id.saveButton).setOnClickListener(v -> {
            String updatedValue = "";
            EditText activeEditText = null;
            switch (currentFieldType) {
                case Constants.TAG_NAME:
                    updatedValue = binding.nameEditText.getText().toString();
                    activeEditText = binding.nameEditText;
                    break;
                case Constants.TAG_USER_NAME:
                    updatedValue = binding.usernameEditText.getText().toString();
                    activeEditText = binding.usernameEditText;
                    break;
                case Constants.TAG_BIO:
                    updatedValue = binding.bioEditText.getText().toString();
                    activeEditText = binding.bioEditText;
                    break;
                case Constants.TAG_MOBILE_NUMBER:
                    updatedValue = binding.mobileNumberEditText.getText().toString();
                    activeEditText = binding.mobileNumberEditText;
                    break;
                case Constants.TAG_GENDER:
                    updatedValue = binding.genderEditText.getText().toString();
                    activeEditText = binding.genderEditText;
                    break;
                case Constants.TAG_DOB:
                    updatedValue = binding.dobEditText.getText().toString();
                    activeEditText = binding.dobEditText;
                    break;
                case Constants.TAG_PAYPAL_ID:
                    updatedValue = binding.paypalIdEditText.getText().toString();
                    activeEditText = binding.paypalIdEditText;
                    break;
                case Constants.TAG_LINK:
                    updatedValue = binding.linkEditText.getText().toString();
                    activeEditText = binding.linkEditText;
                    Log.e(TAG,"updatedValue : " + updatedValue);
                    break;
            }

            if (activeEditText != null && validateInput(updatedValue, activeEditText)) {
                saveField(updatedValue);
            }else if (activeEditText != null) {
                App.makeToast("Validation failed for " + fieldTitle(currentFieldType));
            }
        });

        // Clear text button listeners
        binding.nameSection.findViewById(R.id.clearNameTextButton).setOnClickListener(v -> binding.nameEditText.setText(""));
        binding.usernameSection.findViewById(R.id.clearUsernameTextButton).setOnClickListener(v -> binding.usernameEditText.setText(""));
        // Bio doesn't have a clear button as per screenshot
        binding.mobileNumberSection.findViewById(R.id.clearMobileNumberTextButton).setOnClickListener(v -> binding.mobileNumberEditText.setText(""));
        binding.genderSection.findViewById(R.id.clearGenderTextButton).setOnClickListener(v -> binding.genderEditText.setText(""));
        binding.dobSection.findViewById(R.id.clearDobTextButton).setOnClickListener(v -> binding.dobEditText.setText(""));
        binding.paypalIdSection.findViewById(R.id.clearPaypalIdTextButton).setOnClickListener(v -> binding.paypalIdEditText.setText(""));

        // Special listeners
        binding.bioSection.findViewById(R.id.mentionButton).setOnClickListener(v -> {
            Toast.makeText(this, "Mention button clicked!", Toast.LENGTH_SHORT).show();
        });

        binding.dobSection.findViewById(R.id.dobEditText).setOnClickListener(v -> showDatePickerDialog());

        binding.backArrow.setOnClickListener(v -> {
            finish();
        });

        binding.instagramlinkValue.setOnClickListener(v -> {
            App.preventMultipleClick(binding.instagramlinkValue);
            binding.instaMainLay.setVisibility(View.GONE);
            binding.youtubeMainLay.setVisibility(View.GONE);binding.datalinkSection.setVisibility(View.VISIBLE);
            binding.topBarLink.setVisibility(View.GONE);
            binding.topBar.setVisibility(View.VISIBLE);
            linkFrom = "Instagram";
            Log.e(TAG,"linkEditText : " + GetSet.getInstagram_link());

            binding.linkTitle.setText(getString(R.string.enter_instagram_link));
            binding.linkEditText.setHint(getString(R.string.enter_edittext_for_instagram_link));
            if (!TextUtils.isEmpty(GetSet.getInstagram_link())) {
                binding.linkEditText.setText(GetSet.getInstagram_link());
            } else {
                binding.linkEditText.setText(""); // Ensure it's empty to show hint
            }
        });

        binding.instagramlinkArrow.setOnClickListener(v -> {
            App.preventMultipleClick(binding.instagramlinkValue);
            binding.instaMainLay.setVisibility(View.GONE);
            binding.youtubeMainLay.setVisibility(View.GONE);binding.datalinkSection.setVisibility(View.VISIBLE);
            binding.topBarLink.setVisibility(View.GONE);
            binding.topBar.setVisibility(View.VISIBLE);
            linkFrom = "Instagram";
            Log.e(TAG,"linkEditText : " + GetSet.getInstagram_link());

            binding.linkTitle.setText(getString(R.string.enter_instagram_link));
            binding.linkEditText.setHint(getString(R.string.enter_edittext_for_instagram_link));
            if (!TextUtils.isEmpty(GetSet.getInstagram_link())) {
                binding.linkEditText.setText(GetSet.getInstagram_link());
            } else {
                binding.linkEditText.setText(""); // Ensure it's empty to show hint
            }
        });


        binding.youtubelinkValue.setOnClickListener(v -> {
            App.preventMultipleClick(binding.youtubelinkValue);
            binding.youtubeMainLay.setVisibility(View.GONE);
            binding.instaMainLay.setVisibility(View.GONE);
            binding.datalinkSection.setVisibility(View.VISIBLE);
            binding.topBarLink.setVisibility(View.GONE);
            binding.topBar.setVisibility(View.VISIBLE);
            linkFrom = "Youtube";
            Log.e(TAG,"linkEditText : " + GetSet.getYoutube_link());

            binding.linkTitle.setText(getString(R.string.enter_youtube_link));
            binding.linkEditText.setHint(getString(R.string.enter_edittext_for_youtube_link));
            if (!TextUtils.isEmpty(GetSet.getYoutube_link())) {
                binding.linkEditText.setText(GetSet.getYoutube_link());
            } else {
                binding.linkEditText.setText(""); // Ensure it's empty to show hint
            }
        });

        binding.youtubelinkArrow.setOnClickListener(v -> {
            App.preventMultipleClick(binding.youtubelinkValue);
            binding.youtubeMainLay.setVisibility(View.GONE);
            binding.instaMainLay.setVisibility(View.GONE);
            binding.datalinkSection.setVisibility(View.VISIBLE);
            binding.topBarLink.setVisibility(View.GONE);
            binding.topBar.setVisibility(View.VISIBLE);
            linkFrom = "Youtube";
            Log.e(TAG,"linkEditText : " + GetSet.getYoutube_link());

            binding.linkTitle.setText(getString(R.string.enter_youtube_link));
            binding.linkEditText.setHint(getString(R.string.enter_edittext_for_youtube_link));
            if (!TextUtils.isEmpty(GetSet.getYoutube_link())) {
                binding.linkEditText.setText(GetSet.getYoutube_link());
            } else {
                binding.linkEditText.setText(""); // Ensure it's empty to show hint
            }
        });

    }

    // Char Count Updaters
    private void updateNameCharCount(int currentLength) {
        binding.nameCharCountText.setText(currentLength + "/" + Constants.MAX_LENGTH);
    }

    private void updateUsernameCharCount(int currentLength) {
        binding.usernameCharCountText.setText(currentLength + "/" + Constants.USERNAME_MAX_LENGTH);
    }

    private void updateBioCharCount(int currentLength) {
        binding.bioCharCountText.setText(currentLength + "/" + Constants.BIO_MAX_LENGTH);
    }

    // Validation methods
    private boolean validateInput(String value, EditText editText) {
        if (TextUtils.isEmpty(value)) {
            if (Constants.TAG_NAME.equals(currentFieldType)) {
                editText.setError(getString(R.string.enter_name));
                return false;
            } else if (Constants.TAG_USER_NAME.equals(currentFieldType)) {
                editText.setError(getString(R.string.enter_user_name));
                return false;
            } else if (Constants.TAG_BIO.equals(currentFieldType)) {
                // Bio can be empty
                return true;
            } else if (Constants.TAG_MOBILE_NUMBER.equals(currentFieldType)) {
                editText.setError("Mobile number cannot be empty.");
                return false;
            } else if (Constants.TAG_GENDER.equals(currentFieldType)) {
                editText.setError("Gender cannot be empty.");
                return false;
            } else if (Constants.TAG_DOB.equals(currentFieldType)) {
                editText.setError("Date of Birth cannot be empty.");
                return false;
            } else if (Constants.TAG_PAYPAL_ID.equals(currentFieldType)) {
                editText.setError("PayPal ID cannot be empty.");
                return false;
            }
        }

        if (Constants.TAG_USER_NAME.equals(currentFieldType)) {
            return isValidUsername(value);
        } else if (Constants.TAG_MOBILE_NUMBER.equals(currentFieldType)) {
            return isValidMobileNumber(value);
        } else if (Constants.TAG_PAYPAL_ID.equals(currentFieldType)) {
            return isValidPaypalId(value);
        }
        return true;
    }

    private boolean isValidUsername(String username) {
        return username.length() <= Constants.USERNAME_MAX_LENGTH && username.matches("^[a-z0-9._]+$");
    }

    private boolean isValidMobileNumber(String mobileNumber) {
        return mobileNumber.matches("^[0-9]{10}$");
    }

    private boolean isValidPaypalId(String paypalId) {
        return Patterns.EMAIL_ADDRESS.matcher(paypalId).matches();
    }

    // Date Picker Logic
    private void updateCalendarFromText(String dobText) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            myCalendar.setTime(sdf.parse(dobText));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDobEditText();
            }
        };

        new DatePickerDialog(EditFieldActivity.this,
                dateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateDobEditText() {
        String myFormat = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        binding.dobEditText.setText(sdf.format(myCalendar.getTime()));
        binding.clearDobTextButton.setVisibility(View.VISIBLE);
    }

    // Save Logic
    private void saveField(String updatedValue) {
        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, binding.getRoot(), false);
            return;
        }

        showLoading();

        ProfileRequest request = new ProfileRequest();
        request.setUserId(GetSet.getUserId());
        request.setProfileId(GetSet.getUserId());

        switch (currentFieldType) {
            case Constants.TAG_NAME:
                request.setName(updatedValue);
                break;
            case Constants.TAG_USER_NAME:
                request.setUser_name(updatedValue);
                break;
            case Constants.TAG_BIO:
                request.setBio(updatedValue);
                break;
            case Constants.TAG_MOBILE_NUMBER:
                request.setPhone_number(updatedValue);
                break;
            case Constants.TAG_GENDER:
                request.setGender(updatedValue);
                break;
            case Constants.TAG_DOB:
                request.setDate_of_birth(updatedValue);
                break;
            case Constants.TAG_PAYPAL_ID:
                request.setPayPalId(updatedValue);
                break;
            case Constants.TAG_LINK:
                if(linkFrom.equalsIgnoreCase("Instagram")){
                    request.setInstagramLink(updatedValue);
                }else if(linkFrom.equalsIgnoreCase("Youtube")){
                    request.setYoutubeLink(updatedValue);
                }
                break;
        }

        Call<ProfileResponse> call = apiInterface.getProfile(request);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                        App.makeToast(getString(R.string.profile_updated_successfully));
                        updateGetSetAndSharedPref(profile, updatedValue);
                        linkFrom = "";
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(FIELD_TYPE, currentFieldType);
                        resultIntent.putExtra(FIELD_VALUE, updatedValue);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        App.makeToast(profile.getMessage().toString());
                    }
                } else {
                    App.makeToast("Failed to update profile.");
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                hideLoading();
                call.cancel();
                App.makeToast("Network error: " + t.getMessage());
            }
        });
    }

    private void updateGetSetAndSharedPref(ProfileResponse profile, String updatedValue) {
        switch (currentFieldType) {
            case Constants.TAG_NAME:
                GetSet.setName(updatedValue);
                SharedPref.putString(SharedPref.NAME, updatedValue);
                break;
            case Constants.TAG_USER_NAME:
                GetSet.setUserName(updatedValue);
                SharedPref.putString(SharedPref.USER_NAME, updatedValue);
                break;
            case Constants.TAG_BIO:
                GetSet.setBio(updatedValue);
                SharedPref.putString(SharedPref.BIO, updatedValue);
                break;
            case Constants.TAG_MOBILE_NUMBER:
                GetSet.setGetPhonenumber(updatedValue);
                SharedPref.putString(SharedPref.PHONE_NUMBER, updatedValue);
                break;
            case Constants.TAG_GENDER:
                GetSet.setGender(updatedValue);
                SharedPref.putString(SharedPref.GENDER, updatedValue);
                break;
            case Constants.TAG_DOB:
                GetSet.setDob(updatedValue);
                SharedPref.putString(SharedPref.DOB, updatedValue);
                break;
            case Constants.TAG_PAYPAL_ID:
                GetSet.setPaypal_id(updatedValue);
                SharedPref.putString(SharedPref.PAYPAL_ID, updatedValue);
                break;
            case Constants.TAG_LINK:
                if(linkFrom.equalsIgnoreCase("Instagram")){
                    GetSet.setInstagram_link(updatedValue);
                    SharedPref.putString(SharedPref.INSTAGRAM_LINK, updatedValue);
                }else if(linkFrom.equalsIgnoreCase("Youtube")){
                    GetSet.setYoutube_link(updatedValue);
                    SharedPref.putString(SharedPref.YOUTUBE_LINK, updatedValue);
                }
                break;
        }
    }

    public void showLoading() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, binding.getRoot(), isConnected);
    }
}
