package com.app.alhawanajd;

import static com.app.alhawanajd.App.hideKeyboardFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.app.alhawanajd.R;
import com.app.external.KeyboardUtils;
import com.app.external.MediaPlayerUtils;
import com.app.helper.LocaleManager;
import com.app.helper.callback.OnOkClickListener;
import com.app.model.GetSet;
import com.app.model.SignInRequest;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.bumptech.glide.Glide;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Callback;

import okhttp3.RequestBody;
import okhttp3.MultipartBody;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;

public class DialogProfile extends DialogFragment implements View.OnClickListener {

    private static final String TAG = DialogProfile.class.getSimpleName();
    private TextView txtTitle;
    private TextView txtName;
    private EditText edtName;
    private EditText userName;
    private TextView txtGender;
    private RadioButton btnMale;
//    private RadioButton btnFemale;
    private EditText txtDob;
    private EditText edtDob;
    private LinearLayout profileLayout;
    private Button btnNext;
    private ProgressBar progressBar;

    private DatePickerDialog mDatePickerDialog;
    private String strAge, strDob, strLocation = null;
    private boolean isKeyboardOpen = false, genderSelection = false;
    private OnOkClickListener callBack;
    private Context context;
    private DisplayMetrics displayMetrics;
    private Spinner spinnerGender;
    private ImageView imgProfile, imgEdit;
    private EditText filledTextField;
    private Uri uploadUri;
    private View userNameNotesLay;
    private View userNameLay;
    private View genderLay;
    String[] genderOptions;
    private AppUtils appUtils;
    byte[] imageBytes;
    private InputStream imageStream; // image data
    /*@Override
    public int getTheme() {
        return R.style.MyCustomTheme;
    }*/

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        /*dialog = new Dialog(getActivity(), R.style.MyCustomTheme);*/

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            /*getActivity().getWindow().getAttributes().windowAnimations = R.style.MyCustomTheme;*/
            dialog.getWindow().setWindowAnimations(R.style.AlertDialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;

            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }
    @Override
    public int getTheme() {
        return R.style.FullscreenDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_profile, container, false);

        getDialog().getWindow().setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));


        findViews(itemView);
        displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        edtName.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH_NAME)});
      //  userName.setFilters(new InputFilter[]{AppUtils.SPECIAL_CHARACTERS_FILTER, new InputFilter.LengthFilter(Constants.MAX_LENGTH_NAME)});
        if (SharedPref.getString(SharedPref.FACEBOOK_NAME, null) != null) {
            edtName.setText(SharedPref.getString(SharedPref.FACEBOOK_NAME, null));
           // userName.setText(SharedPref.getString(SharedPref.FACEBOOK_NAME, null));
        }

        appUtils = new AppUtils(context);
        appUtils.pauseExternalAudio();

        progressBar.setIndeterminate(true);
        KeyboardUtils.addKeyboardToggleListener(getActivity(), isVisible -> isKeyboardOpen = isVisible);
        userName.requestFocus();

        if (MediaPlayerUtils.isPlaying()) {
            MediaPlayerUtils.pauseMediaPlayer();
        }

        userName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtName.setGravity(Gravity.START);
                } else {
                    edtName.setGravity(Gravity.END);
                }
            }
        });
        edtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    edtName.setGravity(Gravity.START);
                } else {
                    edtName.setGravity(Gravity.END);
                }
            }
        });
        Log.d(TAG, "onCreateView: type..."+Constants.LOGINMETHODE);
        if (Constants.LOGINMETHODE.equalsIgnoreCase("google")){
            edtName.setText(Constants.LOGIN_NAME);
            userName.setText(Constants.LOGIN_NAME);
        }else {
            edtName.setText("");
            userName.setText("");
        }

        return itemView;

    }

    private void findViews(View view) {
        txtTitle = view.findViewById(R.id.txtTitle);
        txtName = view.findViewById(R.id.txtName);
        edtName = view.findViewById(R.id.edtName);
        txtGender = view.findViewById(R.id.txtGender);
//        btnMale = view.findViewById(R.id.btnMale);
//        btnFemale = view.findViewById(R.id.btnFemale);
        userName = view.findViewById(R.id.edtUserName);
        userNameNotesLay = view.findViewById(R.id.userNameNotesLay);
        userNameLay = view.findViewById(R.id.userNameLay);
        genderLay = view.findViewById(R.id.genderLay);
//        txtDob = view.findViewById(R.id.txtDob);
        edtDob = view.findViewById(R.id.edtDob);
        profileLayout = view.findViewById(R.id.profileLayout);
        btnNext = view.findViewById(R.id.btnNext);
        progressBar = view.findViewById(R.id.progressBar);
        spinnerGender = view.findViewById(R.id.spinnerGender);
        imgProfile = view.findViewById(R.id.imgProfile);
        imgEdit = view.findViewById(R.id.imgEdit);
        filledTextField = view.findViewById(R.id.filledTextField);
        userNameNotesLay.setVisibility(View.GONE);

        genderOptions = getResources().getStringArray(R.array.gender_options);

//        KeyListener customKeyListener = new DigitsKeyListener(false, false) {
//            @Override
//            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//                StringBuilder filtered = new StringBuilder();
//                String allowedChars = "abcdefghijklmnopqrstuvwxyz0123456789._";
//                for (int i = start; i < end; i++) {
//                    char c = Character.toLowerCase(source.charAt(i));
//                    if (allowedChars.indexOf(c) >= 0) {
//                        filtered.append(c);
//                    }
//                }
//
//                return filtered.toString();
//            }
//        };
//
//        userName.setKeyListener(customKeyListener);

       userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               /* String username = s.toString();
                if (!isValidUsername(username)) {
//                    Toasty.error(context, "Only small letters, numbers, dot (.) and underscore (_) allowed. Max 15 chars.", Toasty.LENGTH_SHORT).show();
                    Toasty.error(context, "Only small letters allowed", Toasty.LENGTH_SHORT).show();
                }*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();

                if (username.length() < 4 && !username.isEmpty()) {
                    showUsernameError();
                } else {
                    hideUsernameError();
                }
            }
        });

        // Apply the input filter to restrict input to lowercase letters and numbers
        userName.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                // Loop through each character in the input
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    // Allow only lowercase letters (a-z) and numbers (0-9)
                    if (!Character.isLowerCase(c) && !Character.isDigit(c)) {
                        showUsernameError();
                        return ""; // Reject the character
                    }
                }
                return null; // Accept the input
            }
        }});



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context,
                R.array.gender_options,
                R.layout.spinner_item_white
        );
        adapter.setDropDownViewResource(R.layout.spinner_item_white);
        spinnerGender.setAdapter(adapter);
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                genderLay.setBackgroundResource(R.drawable.bg_input_field);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // This method is called when no item is selected
                // You can leave it empty or handle the case when nothing is selected
            }
        });


    }

    void showUsernameError(){
        userNameNotesLay.setVisibility(View.VISIBLE);
        userNameLay.setBackgroundResource(R.drawable.bg_input_field_error);

    }
    void hideUsernameError(){
        userNameNotesLay.setVisibility(View.GONE);
        userNameLay.setBackgroundResource(R.drawable.bg_input_field);

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initClickListeners();

        if (LocaleManager.isRTL()) {
          // edtName.setGravity(Gravity.END);
        }


    }

    private void initClickListeners() {
        edtDob.setShowSoftInputOnFocus(false);
        edtDob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    Locale.setDefault(Locale.ENGLISH);
                    openDateDialog();
                }
//                hideKeyboardFragment(requireContext(), requireView());
                return true;
            }
        });
//        edtDob.setOnClickListener(this);
        edtDob.setOnClickListener(this);
        btnNext.setOnClickListener(this);
//        btnMale.setOnClickListener(this);
//        btnFemale.setOnClickListener(this);
        edtName.setOnClickListener(this);
        txtName.setOnClickListener(this);
        imgEdit.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickIntent, 101);
        });


    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
//            Uri selectedImageUri = data.getData();
//            if (selectedImageUri != null) {
//                uploadUri = selectedImageUri;
//                imgProfile.setVisibility(View.VISIBLE);
//                Glide.with(context)
//                        .load(uploadUri)
//                        .circleCrop()
//                        .into(imgProfile);
//                try {
//                    imageStream = context.getContentResolver().openInputStream(uploadUri);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                uploadUri = selectedImageUri;
                imgProfile.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(uploadUri)
                        .circleCrop()
                        .into(imgProfile);

                try {
                    InputStream imageStream = context.getContentResolver().openInputStream(uploadUri);
                    imageBytes = AppUtils.getBytes(imageStream); // make sure `AppUtils.getBytes()` exists
                  //  uploadImage(imageBytes); // Call upload method directly
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void uploadImage(byte[] imageBytes) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Map<String, String>> call = apiInterface.uploadProfileImage(body, userid);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> data = response.body();
                    if (Constants.TAG_TRUE.equals(data.get(Constants.TAG_STATUS))) {
                        String userImageUrl = Constants.USER_IMAGE_URL + data.get(Constants.TAG_USER_IMAGE);
                        GetSet.setUserImage(userImageUrl);
                        SharedPref.putString(Constants.TAG_USER_IMAGE, userImageUrl);
                        Toasty.success(context, "Profile image updated").show();
                    } else {
                        Toasty.error(context, "Failed to upload image").show();
                    }
                } else {
                    Toasty.error(context, "Server error").show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                call.cancel();
                Toasty.error(context, "Upload failed: " + t.getMessage()).show();
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edtDob:
//            case R.id.txtDob:
                InputMethodManager imm = (InputMethodManager) edtDob.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(edtDob.getWindowToken(), 0);
                }
                Locale.setDefault(Locale.ENGLISH);
                openDateDialog();
                break;
//            case R.id.btnNext:
//                if (TextUtils.isEmpty(edtName.getText().toString().trim())) {
//                    edtName.getText().clear();
//                    edtName.setError(context.getString(R.string.enter_name));
//                } else if (TextUtils.isEmpty(edtDob.getText().toString().trim())) {
//                    hideKeyboardFragment(requireContext(), requireView());
//                    edtDob.getText().clear();
//                    edtDob.setError(context.getString(R.string.enter_date_of_birth));
//                }
//                else if (!genderSelection) {
//                    if (isKeyboardOpen) {
//                        Toasty.error(context, getResources().getString(R.string.gender_alert), Toasty.LENGTH_SHORT).show();
//                        //  App.makeCustomToast(getString(R.string.gender_alert));
//                    } else {
//                        Toasty.error(context, getResources().getString(R.string.gender_alert), Toasty.LENGTH_SHORT).show();
//                        // App.makeToast(getString(R.string.gender_alert));
//                    }
//                }
//                else {
//                    String languageToLoad  = LocaleManager.getLanguageCode(getActivity()); // your language
//                    Log.d(TAG, "onClick_languageToLoad: "+languageToLoad);
//                    Locale locale = new Locale(languageToLoad);
//                    Locale.setDefault(locale);
//                    Configuration config = new Configuration();
//                    config.locale = locale;
//                    this.getResources().updateConfiguration(config,
//                            this.getResources().getDisplayMetrics());
//                    hideKeyboardFragment(requireContext(), requireView());
//                    edtName.setError(null);
//                    SignInRequest request = new SignInRequest();
//                    request.setLoginId(GetSet.getLoginId());
//                    request.setType(GetSet.getLoginType());
//                    request.setMailId(SharedPref.getString(SharedPref.FACEBOOK_EMAIL, ""));
//                    request.setName(edtName.getText().toString());
//                    request.setAge(strAge);
//                    request.setDob(strDob);
//                    request.setGender(btnMale.isChecked() ? Constants.TAG_MALE : Constants.TAG_FEMALE);
//                    request.setLocation(strLocation);
//                    callBack.onOkClicked(request, "");
//                }
//                break;
            case R.id.btnNext:
                String name = edtName.getText().toString().trim();
                String username2 = userName.getText().toString().trim();
                String dob = edtDob.getText().toString().trim();
                String selectedGender = spinnerGender.getSelectedItem().toString();

                if (TextUtils.isEmpty(username2)) {
                    Toasty.error(context, getResources().getString(R.string.enter_user_name), Toasty.LENGTH_SHORT).show();
                    userNameLay.setBackgroundResource(R.drawable.bg_input_field_error);
                    return;
                }

                if (!isValidUsername(username2)) {
                    Toasty.error(context, getResources().getString(R.string.enter_user_name), Toasty.LENGTH_SHORT).show();
                    userNameLay.setBackgroundResource(R.drawable.bg_input_field_error);
                    return;
                }



                if (TextUtils.isEmpty(name)) {
                    edtName.getText().clear();
                    edtName.setError(context.getString(R.string.enter_name));
                    return;
                }

                if (TextUtils.isEmpty(dob)) {
                    hideKeyboardFragment(requireContext(), requireView());
                    edtDob.getText().clear();
                    edtDob.setError(context.getString(R.string.enter_date_of_birth));
                    return;
                }

                if (selectedGender.equals(genderOptions[0])) {
                    Toasty.error(context, getResources().getString(R.string.gender_alert), Toasty.LENGTH_SHORT).show();
                    genderLay.setBackgroundResource(R.drawable.bg_input_field_error);
                    return;
                }

                // Locale update
                String languageToLoad = LocaleManager.getLanguageCode(getActivity());
                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());

                hideKeyboardFragment(requireContext(), requireView());
                edtName.setError(null);

                // ✅ Create and populate SignInRequest
                SignInRequest request = new SignInRequest();
                request.setLoginId(GetSet.getLoginId());
                request.setType(GetSet.getLoginType());
                request.setMailId(SharedPref.getString(SharedPref.FACEBOOK_EMAIL, ""));
                request.setName(name);
                request.setAge(strAge);
                request.setDob(strDob);
                request.setUsername(userName.getText().toString());
                request.setGender(selectedGender.toLowerCase()); // "male" or "female"
                request.setLocation(strLocation);
                request.setBio(filledTextField.getText().toString().trim());
//                if (uploadUri != null) {
//                    Log.d(TAG, "onClickyesssssss: ");
//                    String base64Image = encodeImageToBase64(uploadUri);
//                    request.setImage(base64Image);
//                }

// if your SignInRequest has this
// ✅ if you added bio field
                // TODO: Add image stream if needed: request.setProfileImage(...);

                // ✅ Send result back
//                callBack.onOkClicked(request, "");
                callBack.onOkClicked(request, uploadUri != null ? encodeImageToBase64(uploadUri) : "");

                break;

//            case R.id.btnMale:
//                genderSelection = true;
////                edtName.setCursorVisible(false);
//                edtName.clearFocus();
//                if (isKeyboardOpen) {
//                    Toasty.error(context, getResources().getString(R.string.male), Toasty.LENGTH_SHORT).show();
//                    //App.makeCustomToast(getString(R.string.male));
//                } else {
//                    Toasty.error(context, getResources().getString(R.string.male), Toasty.LENGTH_SHORT).show();
//                    //  App.makeToast(getString(R.string.male));
//                }
//                break;
//            case R.id.btnFemale:
//                genderSelection = true;
////                edtName.setCursorVisible(false);
//                edtName.clearFocus();
//                if (isKeyboardOpen) {
//                    //App.makeCustomToast(getString(R.string.female));
//                    Toasty.error(context, getResources().getString(R.string.female), Toasty.LENGTH_SHORT).show();
//                } else {
//                    Toasty.error(context, getResources().getString(R.string.female), Toasty.LENGTH_SHORT).show();
//                    //App.makeToast(getString(R.string.female));
//                }
//                break;

            case R.id.edtName:
            case R.id.txtName:
//                edtName.setCursorVisible(true);
                edtName.requestFocus();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*setStyle(STYLE_NO_TITLE, R.style.AlertDialog);*/


    }


    public boolean isValidUsername(String username) {
//        if (username.length() > 15) return false; // Optional limit
//        return username.matches("^[a-z0-9._]+$");
        return true;
    }
    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    private void openDateDialog() {
        String languageToLoad  = "en"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        this.getResources().updateConfiguration(config,
                this.getResources().getDisplayMetrics());

        Locale.setDefault(Locale.ENGLISH);
        //Locale locale = new Locale("en");
        //Locale.setDefault(locale);

        Calendar calendar = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();


        cal.set(cal.get(Calendar.YEAR) - Constants.MIN_AGE, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        long maxDate = cal.getTimeInMillis();
        calendar.setTimeInMillis(maxDate);

            mDatePickerDialog = new DatePickerDialog(context, R.style.DatePickerTheme, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Locale.setDefault(Locale.ENGLISH);
                Locale locale = new Locale("en");
                Locale.setDefault(locale);
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(year, monthOfYear, dayOfMonth);
                final Date newDate = newCalendar.getTime();

                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

                strAge = "" + AppUtils.calculateAge(newDate);
                strDob = displayFormat.format(newDate);
                edtDob.setText(strDob);
                edtDob.setError(null);
                if (TextUtils.isEmpty(edtName.getText())) {
//                    edtName.setCursorVisible(true);
                    edtName.requestFocus();
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (TextUtils.isEmpty(edtDob.getText())) {
                    edtDob.requestFocus();
                } else {
                    edtDob.setCursorVisible(false);
                    if (TextUtils.isEmpty(edtName.getText())) {
                        edtName.requestFocus();
                    }
                }
            }
        });
        mDatePickerDialog.getDatePicker().setMaxDate(maxDate);
        mDatePickerDialog.getDatePicker().getTouchables().get(0).performClick();
        mDatePickerDialog.show();
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        btnNext.setEnabled(false);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnNext.setEnabled(true);
        if (getActivity() != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
