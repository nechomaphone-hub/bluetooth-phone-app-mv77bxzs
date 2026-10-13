package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private BluetoothManagerHelper bluetoothManager;
    private FavoritesManager favoritesManager;

    // UI Root container
    private LinearLayout rootLayout;
    private TextView tvStatus, tvClock, tvDeviceName;
    private LinearLayout contentContainer;

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClockAndDate();
            clockHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothManager = new BluetoothManagerHelper(this);
        favoritesManager = new FavoritesManager(this);

        checkAndRequestPermissions();

        // Build main multimedia dashboard UI layout programmatically with RTL support
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(0xFF111827); // Dark sophisticated background
        rootLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        rootLayout.setPadding(24, 24, 24, 24);

        buildHeader();
        buildNavigationTabs();

        contentContainer = new LinearLayout(this);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        contentContainer.setLayoutParams(contentParams);
        rootLayout.addView(contentContainer);

        setContentView(rootLayout);

        clockHandler.post(clockRunnable);

        // Load Home dashboard initially
        showHomeDashboard();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clockHandler.removeCallbacks(clockRunnable);
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            String[] perms = {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG
            };
            requestPermissions(perms, 100);
        } else {
            String[] perms = {
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG
            };
            requestPermissions(perms, 100);
        }
    }

    private void buildHeader() {
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerLayout.setPadding(16, 16, 16, 16);
        headerLayout.setBackgroundResource(R.drawable.bg_card);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout statusLayout = new LinearLayout(this);
        statusLayout.setOrientation(LinearLayout.VERTICAL);
        statusLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        tvStatus = new TextView(this);
        tvStatus.setText(R.string.bt_status_disconnected);
        tvStatus.setTextColor(0xFFEF4444); // Red warning
        tvStatus.setTextSize(16);
        tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
        statusLayout.addView(tvStatus);

        tvDeviceName = new TextView(this);
        tvDeviceName.setText("לא נבחר מכשיר משני");
        tvDeviceName.setTextColor(0xFF9CA3AF);
        tvDeviceName.setTextSize(14);
        statusLayout.addView(tvDeviceName);

        headerLayout.addView(statusLayout);

        tvClock = new TextView(this);
        tvClock.setTextColor(0xFFF3F4F6);
        tvClock.setTextSize(18);
        tvClock.setTypeface(null, android.graphics.Typeface.BOLD);
        tvClock.setGravity(Gravity.CENTER);
        headerLayout.addView(tvClock);

        rootLayout.addView(headerLayout);

        // Spacer
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16));
        rootLayout.addView(spacer);
    }

    private void buildNavigationTabs() {
        LinearLayout navLayout = new LinearLayout(this);
        navLayout.setOrientation(LinearLayout.HORIZONTAL);
        navLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        navLayout.setPadding(8, 8, 8, 8);
        navLayout.setBackgroundResource(R.drawable.bg_card);

        navLayout.addView(createNavButton(getString(R.string.nav_dialer), v -> showDialerScreen()));
        navLayout.addView(createNavButton(getString(R.string.nav_contacts), v -> showContactsScreen()));
        navLayout.addView(createNavButton(getString(R.string.nav_recent), v -> showRecentCallsScreen()));
        navLayout.addView(createNavButton(getString(R.string.nav_favorites), v -> showFavoritesScreen()));
        navLayout.addView(createNavButton("Bluetooth", v -> showBluetoothDevicesScreen()));
        navLayout.addView(createNavButton(getString(R.string.nav_settings), v -> showSettingsScreen()));

        rootLayout.addView(navLayout);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16));
        rootLayout.addView(spacer);
    }

    private Button createNavButton(String title, View.OnClickListener listener) {
        Button btn = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 70, 1.0f);
        params.setMargins(4, 0, 4, 0);
        btn.setLayoutParams(params);
        btn.setText(title);
        btn.setTextColor(0xFFFFFFFF);
        btn.setTextSize(13);
        btn.setBackgroundResource(R.drawable.bg_button_primary);
        btn.setOnClickListener(listener);
        return btn;
    }

    private void updateClockAndDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss\ndd/MM/yyyy", Locale.getDefault());
        tvClock.setText(sdf.format(new Date()));
    }

    // --- SCREENS ---

    private void showHomeDashboard() {
        contentContainer.removeAllViews();

        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        TextView welcome = new TextView(this);
        welcome.setText("ברוכים הבאים למערכת Bluetooth Phone למולטימדיה לרכב");
        welcome.setTextColor(0xFF38BDF8);
        welcome.setTextSize(20);
        welcome.setTypeface(null, android.graphics.Typeface.BOLD);
        welcome.setPadding(0, 0, 0, 24);
        layout.addView(welcome);

        // Connection Control Card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card);
        card.setPadding(24, 24, 24, 24);

        TextView cardTitle = new TextView(this);
        cardTitle.setText("ניהול חיבור טלפון משני (כשר / חכם)");
        cardTitle.setTextColor(0xFFFFFFFF);
        cardTitle.setTextSize(16);
        cardTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(cardTitle);

        View sp = new View(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16));
        card.addView(sp);

        Button btnConn = new Button(this);
        btnConn.setText(bluetoothManager.isConnected() ? getString(R.string.disconnect_btn) : getString(R.string.connect_btn));
        btnConn.setTextColor(0xFFFFFFFF);
        btnConn.setBackgroundResource(bluetoothManager.isConnected() ? R.drawable.bg_button_danger : R.drawable.bg_button_call);
        btnConn.setOnClickListener(v -> {
            if (bluetoothManager.isConnected()) {
                bluetoothManager.disconnect();
                tvStatus.setText(R.string.bt_status_disconnected);
                tvStatus.setTextColor(0xFFEF4444);
                tvDeviceName.setText("לא נבחר מכשיר משני");
                Toast.makeText(this, "החיבור נותק", Toast.LENGTH_SHORT).show();
            } else {
                showBluetoothDevicesScreen();
            }
            showHomeDashboard();
        });
        card.addView(btnConn);

        layout.addView(card);
        sv.addView(layout);
        contentContainer.addView(sv);
    }

    private void showDialerScreen() {
        contentContainer.removeAllViews();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setPadding(24, 16, 24, 16);

        // CRITICAL REQUIREMENT: Number display strictly LTR
        final EditText etNumber = new EditText(this);
        etNumber.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 90));
        etNumber.setTextDirection(View.TEXT_DIRECTION_LTR);
        etNumber.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        etNumber.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        etNumber.setTextColor(0xFFFFFFFF);
        etNumber.setTextSize(26);
        etNumber.setBackgroundResource(R.drawable.bg_card);
        etNumber.setHint(getString(R.string.dialer_hint));
        etNumber.setHintTextColor(0xFF6B7280);
        layout.addView(etNumber);

        View sp = new View(this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16));
        layout.addView(sp);

        // Keypad Grid 3x4
        String[][] keys = {
                {"1", "2", "3"},
                {"4", "5", "6"},
                {"7", "8", "9"},
                {"*", "0", "#"}
        };

        for (String[] rowKeys : keys) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            rowLayout.setPadding(0, 4, 0, 4);

            for (String key : rowKeys) {
                Button btnKey = new Button(this);
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                btnParams.setMargins(4, 0, 4, 0);
                btnKey.setLayoutParams(btnParams);
                btnKey.setText(key);
                btnKey.setTextColor(0xFFFFFFFF);
                btnKey.setTextSize(24);
                btnKey.setBackgroundResource(R.drawable.bg_button_key);
                btnKey.setOnClickListener(v -> {
                    String current = etNumber.getText().toString();
                    etNumber.setText(current + key);
                    etNumber.setSelection(etNumber.getText().length());
                });
                rowLayout.addView(btnKey);
            }
            layout.addView(rowLayout);
        }

        // Action Buttons Row (Call, Delete, Clear, Paste)
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 70));
        actionRow.setPadding(0, 16, 0, 0);

        Button btnClear = new Button(this);
        btnClear.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        btnClear.setText(getString(R.string.clear_button));
        btnClear.setTextColor(0xFFFFFFFF);
        btnClear.setBackgroundResource(R.drawable.bg_button_danger);
        btnClear.setOnClickListener(v -> etNumber.setText(""));
        actionRow.addView(btnClear);

        Button btnDel = new Button(this);
        LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        delParams.setMargins(8, 0, 8, 0);
        btnDel.setLayoutParams(delParams);
        btnDel.setText(getString(R.string.delete_button));
        btnDel.setTextColor(0xFFFFFFFF);
        btnDel.setBackgroundResource(R.drawable.bg_button_key);
        btnDel.setOnClickListener(v -> {
            String current = etNumber.getText().toString();
            if (!current.isEmpty()) {
                etNumber.setText(current.substring(0, current.length() - 1));
                etNumber.setSelection(etNumber.getText().length());
            }
        });
        actionRow.addView(btnDel);

        Button btnCall = new Button(this);
        btnCall.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f));
        btnCall.setText(getString(R.string.call_button));
        btnCall.setTextColor(0xFFFFFFFF);
        btnCall.setBackgroundResource(R.drawable.bg_button_call);
        btnCall.setOnClickListener(v -> {
            String phoneNum = etNumber.getText().toString().trim();
            if (phoneNum.isEmpty()) {
                Toast.makeText(this, "אנא הזן מספר חיוג תקין", Toast.LENGTH_SHORT).show();
                return;
            }
            performCall(phoneNum);
        });
        actionRow.addView(btnCall);

        layout.addView(actionRow);
        contentContainer.addView(layout);
    }

    private void performCall(String phoneNumber) {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            try {
                startActivity(callIntent);
                Toast.makeText(this, "מבצע חיוג אל: " + phoneNumber, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "שגיאה בביצוע חיוג: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "אין הרשאת חיוג טלפוני", Toast.LENGTH_SHORT).show();
        }
    }

    private void showContactsScreen() {
        contentContainer.removeAllViews();

        ScrollView sv = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        List<ContactModel> contacts = loadDeviceContacts();
        if (contacts.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.empty_contacts));
            empty.setTextColor(0xFF9CA3AF);
            empty.setTextSize(16);
            layout.addView(empty);
        } else {
            for (ContactModel contact : contacts) {
                LinearLayout item = new LinearLayout(this);
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                item.setPadding(16, 16, 16, 16);
                item.setBackgroundResource(R.drawable.bg_card);
                item.setGravity(Gravity.CENTER_VERTICAL);
                
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) item.getLayoutParams();
                p.setMargins(0, 0, 0, 8);

                LinearLayout info = new LinearLayout(this);
                info.setOrientation(LinearLayout.VERTICAL);
                info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

                TextView tvName = new TextView(this);
                tvName.setText(contact.getName());
                tvName.setTextColor(0xFFFFFFFF);
                tvName.setTextSize(16);
                tvName.setTypeface(null, android.graphics.Typeface.BOLD);
                info.addView(tvName);

                TextView tvNum = new TextView(this);
                tvNum.setText(contact.getPhoneNumber());
                tvNum.setTextColor(0xFF38BDF8);
                tvNum.setTextSize(14);
                tvNum.setTextDirection(View.TEXT_DIRECTION_LTR);
                info.addView(tvNum);

                item.addView(info);

                Button btnCallContact = new Button(this);
                btnCallContact.setText("חיוג");
                btnCallContact.setTextColor(0xFFFFFFFF);
                btnCallContact.setBackgroundResource(R.drawable.bg_button_call);
                btnCallContact.setOnClickListener(v -> performCall(contact.getPhoneNumber()));
                item.addView(btnCallContact);

                Button btnFav = new Button(this);
                boolean isFav = favoritesManager.isFavorite(contact.getPhoneNumber());
                btnFav.setText(isFav ? "★" : "☆");
                btnFav.setTextColor(0xFFFBBF24);
                btnFav.setBackgroundResource(R.drawable.bg_button_key);
                btnFav.setOnClickListener(v -> {
                    if (favoritesManager.isFavorite(contact.getPhoneNumber())) {
                        favoritesManager.removeFavorite(contact.getPhoneNumber());
                        Toast.makeText(this, "הוסר ממועדפים", Toast.LENGTH_SHORT).show();
                    } else {
                        favoritesManager.addFavorite(contact.getPhoneNumber());
                        Toast.makeText(this, "נוסף למועדפים", Toast.LENGTH_SHORT).show();
                    }
                    showContactsScreen();
                });
                item.addView(btnFav);

                layout.addView(item);
            }
        }

        sv.addView(layout);
        contentContainer.addView(sv);
    }

    private List<ContactModel> loadDeviceContacts() {
        List<ContactModel> list = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return list;
        }

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (nameIdx >= 0 && numIdx >= 0) {
                    String name = cursor.getString(nameIdx);
                    String number = cursor.getString(numIdx);
                    list.add(new ContactModel(name, number));
                }
            }
            cursor.close();
        }
        return list;
    }

    private void showRecentCallsScreen() {
        contentContainer.removeAllViews();

        ScrollView sv = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        List<CallRecordModel> records = loadCallLogs();
        if (records.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.empty_recent));
            empty.setTextColor(0xFF9CA3AF);
            empty.setTextSize(16);
            layout.addView(empty);
        } else {
            for (CallRecordModel rec : records) {
                LinearLayout item = new LinearLayout(this);
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                item.setPadding(16, 16, 16, 16);
                item.setBackgroundResource(R.drawable.bg_card);
                item.setGravity(Gravity.CENTER_VERTICAL);

                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) item.getLayoutParams();
                p.setMargins(0, 0, 0, 8);

                LinearLayout info = new LinearLayout(this);
                info.setOrientation(LinearLayout.VERTICAL);
                info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

                TextView tvType = new TextView(this);
                tvType.setText(rec.getType() + " • " + rec.getDate());
                tvType.setTextColor(0xFF9CA3AF);
                tvType.setTextSize(12);
                info.addView(tvType);

                TextView tvNum = new TextView(this);
                tvNum.setText(rec.getNameOrNumber());
                tvNum.setTextColor(0xFFFFFFFF);
                tvNum.setTextSize(16);
                tvNum.setTextDirection(View.TEXT_DIRECTION_LTR);
                info.addView(tvNum);

                item.addView(info);

                Button btnCall = new Button(this);
                btnCall.setText("חיוג חוזר");
                btnCall.setTextColor(0xFFFFFFFF);
                btnCall.setBackgroundResource(R.drawable.bg_button_call);
                btnCall.setOnClickListener(v -> performCall(rec.getNameOrNumber()));
                item.addView(btnCall);

                layout.addView(item);
            }
        }

        sv.addView(layout);
        contentContainer.addView(sv);
    }

    private List<CallRecordModel> loadCallLogs() {
        List<CallRecordModel> list = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return list;
        }

        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null, null, null,
                CallLog.Calls.DATE + " DESC LIMIT 50"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE);
                int typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE);

                if (numIdx >= 0 && dateIdx >= 0 && typeIdx >= 0) {
                    String number = cursor.getString(numIdx);
                    long dateTime = cursor.getLong(dateIdx);
                    int callTypeCode = cursor.getInt(typeIdx);

                    String typeStr = "שיחה";
                    if (callTypeCode == CallLog.Calls.INCOMING_TYPE) typeStr = "נכנסת";
                    else if (callTypeCode == CallLog.Calls.OUTGOING_TYPE) typeStr = "יוצאת";
                    else if (callTypeCode == CallLog.Calls.MISSED_TYPE) typeStr = "לא נענתה";

                    String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(dateTime));
                    list.add(new CallRecordModel(number, dateStr, typeStr));
                }
            }
            cursor.close();
        }
        return list;
    }

    private void showFavoritesScreen() {
        contentContainer.removeAllViews();

        ScrollView sv = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        List<String> favs = favoritesManager.getFavorites();
        if (favs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.empty_favorites));
            empty.setTextColor(0xFF9CA3AF);
            empty.setTextSize(16);
            layout.addView(empty);
        } else {
            for (String num : favs) {
                LinearLayout item = new LinearLayout(this);
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                item.setPadding(16, 16, 16, 16);
                item.setBackgroundResource(R.drawable.bg_card);
                item.setGravity(Gravity.CENTER_VERTICAL);

                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) item.getLayoutParams();
                p.setMargins(0, 0, 0, 8);

                TextView tvNum = new TextView(this);
                tvNum.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
                tvNum.setText(num);
                tvNum.setTextColor(0xFFFFFFFF);
                tvNum.setTextSize(18);
                tvNum.setTextDirection(View.TEXT_DIRECTION_LTR);
                item.addView(tvNum);

                Button btnCall = new Button(this);
                btnCall.setText("חיוג");
                btnCall.setTextColor(0xFFFFFFFF);
                btnCall.setBackgroundResource(R.drawable.bg_button_call);
                btnCall.setOnClickListener(v -> performCall(num));
                item.addView(btnCall);

                Button btnRemove = new Button(this);
                btnRemove.setText("הסר");
                btnRemove.setTextColor(0xFFFFFFFF);
                btnRemove.setBackgroundResource(R.drawable.bg_button_danger);
                btnRemove.setOnClickListener(v -> {
                    favoritesManager.removeFavorite(num);
                    Toast.makeText(this, "הסר ממועדפים", Toast.LENGTH_SHORT).show();
                    showFavoritesScreen();
                });
                item.addView(btnRemove);

                layout.addView(item);
            }
        }

        sv.addView(layout);
        contentContainer.addView(sv);
    }

    private void showBluetoothDevicesScreen() {
        contentContainer.removeAllViews();

        ScrollView sv = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        TextView title = new TextView(this);
        title.setText("מכשירים משויכים (Bluetooth)");
        title.setTextColor(0xFF38BDF8);
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 16);
        layout.addView(title);

        List<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevices();
        if (pairedDevices.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("אין מכשירים משויכים. אנא צמד את הטלפון השני דרך הגדרות מערכת Android.");
            empty.setTextColor(0xFF9CA3AF);
            empty.setTextSize(16);
            layout.addView(empty);
        } else {
            for (BluetoothDevice device : pairedDevices) {
                LinearLayout item = new LinearLayout(this);
                item.setOrientation(LinearLayout.HORIZONTAL);
                item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                item.setPadding(16, 16, 16, 16);
                item.setBackgroundResource(R.drawable.bg_card);
                item.setGravity(Gravity.CENTER_VERTICAL);

                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) item.getLayoutParams();
                p.setMargins(0, 0, 0, 8);

                LinearLayout info = new LinearLayout(this);
                info.setOrientation(LinearLayout.VERTICAL);
                info.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

                String name = "מכשיר לא ידוע";
                try {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        name = device.getName();
                        if (name == null) name = "מכשיר ללא שם";
                    }
                } catch (SecurityException ignored) {}

                TextView tvDevName = new TextView(this);
                tvDevName.setText(name);
                tvDevName.setTextColor(0xFFFFFFFF);
                tvDevName.setTextSize(16);
                tvDevName.setTypeface(null, android.graphics.Typeface.BOLD);
                info.addView(tvDevName);

                TextView tvMac = new TextView(this);
                tvMac.setText(device.getAddress());
                tvMac.setTextColor(0xFF9CA3AF);
                tvMac.setTextSize(12);
                tvMac.setTextDirection(View.TEXT_DIRECTION_LTR);
                info.addView(tvMac);

                item.addView(info);

                Button btnConnect = new Button(this);
                btnConnect.setText("חיבור למערכת");
                btnConnect.setTextColor(0xFFFFFFFF);
                btnConnect.setBackgroundResource(R.drawable.bg_button_primary);
                final String finalName = name;
                btnConnect.setOnClickListener(v -> {
                    boolean success = bluetoothManager.connectToDevice(device);
                    if (success) {
                        tvStatus.setText(getString(R.string.bt_status_connected));
                        tvStatus.setTextColor(0xFF10B981);
                        tvDeviceName.setText(finalName);
                        Toast.makeText(this, "התחבר בהצלחה אל " + finalName, Toast.LENGTH_SHORT).show();
                        showHomeDashboard();
                    } else {
                        Toast.makeText(this, "החיבור נכשל", Toast.LENGTH_SHORT).show();
                    }
                });
                item.addView(btnConnect);

                layout.addView(item);
            }
        }

        sv.addView(layout);
        contentContainer.addView(sv);
    }

    private void showSettingsScreen() {
        contentContainer.removeAllViews();

        ScrollView sv = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        TextView cardTitle = new TextView(this);
        cardTitle.setText(getString(R.string.limitations_title));
        cardTitle.setTextColor(0xFF38BDF8);
        cardTitle.setTextSize(18);
        cardTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        cardTitle.setPadding(0, 0, 0, 16);
        layout.addView(cardTitle);

        TextView cardDesc = new TextView(this);
        cardDesc.setText(getString(R.string.limitations_desc));
        cardDesc.setTextColor(0xFFD1D5DB);
        cardDesc.setTextSize(15);
        cardDesc.setLineSpacing(4.0f, 1.2f);
        cardDesc.setBackgroundResource(R.drawable.bg_card);
        cardDesc.setPadding(24, 24, 24, 24);
        layout.addView(cardDesc);

        sv.addView(layout);
        contentContainer.addView(sv);
    }
}
