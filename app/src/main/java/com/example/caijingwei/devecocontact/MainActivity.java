package com.example.caijingwei.devecocontact;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.ContactsContract;
//import android.support.v4.content.PermissionChecker;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import android.provider.ContactsContract.Groups;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

class CreateContacts {
    private static final String TAG = "CreateContacts";
    private static final String GROUP_NAME = "DevEco";
    private static final long INVALID_GROUP_ID = -1;

    private Handler mUIHandler = null;
    private Context mContext;

    private CreateContacts() {}
    public CreateContacts(Context context, Handler UIHandler) {
        mContext = context;
        mUIHandler = UIHandler;
    }

    private void addMessage(String msg) {
        Message newMsg = Message.obtain();
        newMsg.what = MainActivity.THREAD_MESSAGE_WHAT_INFO;
        Bundle bundle = new Bundle();
        bundle.putString("info", msg);
        newMsg.setData(bundle);
        mUIHandler.sendMessage(newMsg);
    }


    //查询指定电话的联系人姓名，邮箱
    boolean findContactNameByNumber(String number, String name){
        // this is partial match, should match precise phone number later
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = mContext.getContentResolver();

        // question: for contact#1 with 123456, contact#2 with 12345678, when filter/123456
        //           should return 2 contacts or only contact#1?
        //           during test, when there is only contact#2, contact#2 is returned;
        //                        when there are contact#1 and contact#2, only contact#1 is returned;
//        Cursor cursorName = resolver.query(uri, new String[]{"display_name"}, null, null, null);
//        while (cursorName.moveToNext()) {
//             String resultName = cursorName.getString(0);
//            Log.i(TAG, resultName);
//        }
//        cursorName.close();

        //data1存储各个记录的总数据，mimetype存放记录的类型，如电话、email等
        Cursor cursor = resolver.query(uri, new String[]{"data1", "mimetype"}, null, null, null);
        while (cursor.moveToNext()) {
            String data = cursor.getString(cursor.getColumnIndex("data1"));
            if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/phone_v2")) { //如果是电话
                cursor.close();
                // data may contain non-digit characters
                String newData = "";
                for (int i = 0; i < data.length(); ++i) {
                    if (Character.isDigit(data.charAt(i))) {
                        newData += data.charAt(i);
                    }
                }
                return (newData.equalsIgnoreCase(number));
            }
//            if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/name")) {    //如果是名字
//                buf.append(",name=" + data);
//            } else if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/phone_v2")) { //如果是电话
//                buf.append(",phone=" + data);
//            } else if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/email_v2")) { //如果是email
//                buf.append(",email=" + data);
//            } else if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/postal-address_v2")) { //如果是地址
//                buf.append(",address=" + data);
//            } else if (cursor.getString(cursor.getColumnIndex("mimetype")).equals("vnd.android.cursor.item/organization")) { //如果是组织
//                buf.append(",organization=" + data);
//            }
        }
        cursor.close();
        return false;
    }

    public void execute() {
        addMessage("开始创建联系人");

        // read from excel
        ArrayList<ParseExcel.Person> persons = null;
        try {
            persons = ParseExcel.parse(mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // test code
//        Log.d(TAG, String.valueOf(findContactNameByNumber("13621122345", "蔡经伟")));
//        Log.d(TAG, String.valueOf(findContactNameByNumber("13001230113", "林孟")));
//        Log.d(TAG, String.valueOf(findContactNameByNumber("13001230113", "ttt")));
//        Log.d(TAG, String.valueOf(findContactNameByNumber("234234", "test")));

        // get or create group
        long groupId = findAllGroupInfo();
        if (groupId == INVALID_GROUP_ID) {
            groupId = createGroup();
        }
        Log.d("contact", String.format("group id: %d", groupId));
        if (groupId == INVALID_GROUP_ID) {
            return;
        }
        String stringGroupId = String.valueOf(groupId);

        // create each contact
        for (int i = 0; i < persons.size(); ++i) {
            ParseExcel.Person person = persons.get(i);
            addContact(person.number, person.name, person.role, person.birthday, stringGroupId);
        }

        // test code
//        addContact("13811803570", "李晓璐", "业务负责人", "3月29日", stringGroupId);
//        addContact("654321", "aaa234", "负责人", "11月29日", stringGroupId);
//        addContact("123456", "aaa123", "owner", "33月29日", stringGroupId);

        addMessage("运行结束");
        return;
    }

    long findAllGroupInfo() {
        Log.d("contact", "findAllGroupInfo()");
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(Groups.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {
                int groupId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Groups._ID));
                String groupName = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));
                Log.i("contact", "group id:" + groupId + ">>groupName:" + groupName);
                if (groupName.equalsIgnoreCase(GROUP_NAME)) {
                    return groupId;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return INVALID_GROUP_ID;
    }

    long createGroup() {
        Log.d("contact", "createGroup()");
        ContentValues value = new ContentValues();
        value.put(Groups.TITLE, GROUP_NAME);
        Uri uri = mContext.getContentResolver().insert(Groups.CONTENT_URI, value);
        return ContentUris.parseId(uri);
    }

    /**
     * 添加联系人
     * 在同一个事务中完成联系人各项数据的添加
     * 使用ArrayList<ContentProviderOperation>，把每步操作放在它的对象中执行
     */
    boolean addContact(String number, String name, String role, String birthday, String groupId) {
        if (findContactNameByNumber(number, name)) {
            Log.d(TAG, String.format("NOT add, found: %s - %s", number, name));
            addMessage(String.format("已经存在，不添加：%s", name));
            return false;
        }
        Log.d(TAG, String.format("will add: %s - %s", number, name));

        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = mContext.getContentResolver();
        // 第一个参数：内容提供者的主机名
        // 第二个参数：要执行的操作
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation operation1 = ContentProviderOperation.newInsert(uri)
                .withValue("account_name", null)
                .build();

        // 操作2.添加data表中name字段
        uri = Uri.parse("content://com.android.contacts/data");
        ContentProviderOperation operation2 = ContentProviderOperation.newInsert(uri)
                // 第二个参数int previousResult:表示上一个操作的位于operations的第0个索引，
                // 所以能够将上一个操作返回的raw_contact_id作为该方法的参数
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/name")
                .withValue("data2", name)
                .build();

        // 操作3.添加data表中phone字段
        uri = Uri.parse("content://com.android.contacts/data");
        ContentProviderOperation operation3 = ContentProviderOperation.newInsert(uri)
                .withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/phone_v2")
                .withValue("data2", "2")
                .withValue("data1", number)
                .build();

        // 操作4.添加data表中的Email字段
//        uri = Uri.parse("content://com.android.contacts/data");
//        ContentProviderOperation operation4 = ContentProviderOperation
//                .newInsert(uri).withValueBackReference("raw_contact_id", 0)
//                .withValue("mimetype", "vnd.android.cursor.item/email_v2")
//                .withValue("data2", "2")
//                .withValue("data1", "zhouguoping@qq.com").build();
//
        ContentProviderOperation operation5 = ContentProviderOperation
                .newInsert(uri).withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/contact_event")
                // public static final int TYPE_BIRTHDAY = 3;
                .withValue("data2", "3")
                .withValue("data1", birthday).build();

        ContentProviderOperation operation6 = ContentProviderOperation
                .newInsert(uri).withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", "vnd.android.cursor.item/organization")
                .withValue("data1", "小米 - " + role)   // because contact card only displays company, not role title
                .withValue("data2", "1")    // work
//                .withValue("data4", role)
                .build();

        ContentProviderOperation operation7 = ContentProviderOperation
                .newInsert(uri).withValueBackReference("raw_contact_id", 0)
                .withValue("mimetype", ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                .build();

        operations.add(operation1);
        operations.add(operation2);
        operations.add(operation3);
//        operations.add(operation4);
        operations.add(operation5);
        operations.add(operation6);
        operations.add(operation7);

        try {
            ContentProviderResult[] results =  resolver.applyBatch("com.android.contacts", operations);
            Log.d(TAG, results.toString());
            if (results.length == 0) {
                addMessage(String.format("创建失败：%s", name));
            } else {
                addMessage(String.format("创建成功：%s", name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE = 0; // request code

    static final String[] PERMISSIONS = new String[] {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
    };
    private PermissionsChecker mPermissionsChecker;

    private TextListView mListView;
    public static final int THREAD_MESSAGE_WHAT_INFO = 1;
    public static final int THREAD_MESSAGE_WHAT_END = 2;
    private HandlerThread mThread = new HandlerThread("contact");
    private Handler mThreadHandler;
    private Handler mUIHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case THREAD_MESSAGE_WHAT_INFO:
                    mListView.addMessage(message.getData().getString("info"));
                    break;
                case THREAD_MESSAGE_WHAT_END:
                    ((Button)findViewById(R.id.buttonStart)).setEnabled(true);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPermissionsChecker = new PermissionsChecker(this);
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }

        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText(Html.fromHtml("<a href='http://www.ecaijw.com'>有问题联系蔡经伟，或点击拨打电话</a> "));
        textView.setClickable(true);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //用intent启动拨打电话
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:18610123248"));
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(intent);
            }
        });

        mListView = (TextListView) findViewById(R.id.listview);
        mListView.addMessage("*****************************");
        mListView.addMessage("  2) 如果通讯录有多个账户，默认在第一个账户中创建");
        mListView.addMessage("  1) 手机号如果已经存在，不会更新生日等信息");
        mListView.addMessage("已知问题：");
        mListView.addMessage("自动在手机通讯录中创建团队所有童鞋的手机号码、生日、职责等信息");
        mListView.addMessage("点击【创建联系人】按钮");
        mListView.addMessage("MIUI生态开发团队通讯录工具");
        mListView.addMessage("****************************");

        // only start() once
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                CreateContacts createContacts = new CreateContacts(MainActivity.this, mUIHandler);
                createContacts.execute();

                mUIHandler.sendEmptyMessage(THREAD_MESSAGE_WHAT_END);
                return false;
            }
        });

        ((Button)findViewById(R.id.buttonStart)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // disable button to avoid re-enter
                ((Button)findViewById(R.id.buttonStart)).setEnabled(false);
                // send the message to trigger to create contacts
                mThreadHandler.sendEmptyMessage(0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThread.quit();
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // when deny, quit the app because of lacking permissions
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
    }
}
