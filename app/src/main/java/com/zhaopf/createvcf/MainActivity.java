package com.zhaopf.createvcf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import a_vcard.android.syncml.pim.vcard.VCardException;

/**
 * 根据规则批量创建Vcf联系人文件
 *
 * @author zhao-pf 2019/11/19
 * Github:https://github.com/zhao-pf/VcfCreate
 */

public class MainActivity extends AppCompatActivity {


    public static final String VCARD_FILE_PATH = Environment.getExternalStorageDirectory() + "/Download";
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button button = findViewById(R.id.createVcard);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText startNumber = findViewById(R.id.et_startNumber);//开始号码
                EditText number = findViewById(R.id.et_number);//生成数量

                if (startNumber.getText().toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, "号码为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (number.getText().toString().trim().equals("")) {
                        Toast.makeText(MainActivity.this, "数量为空", Toast.LENGTH_SHORT).show();
                    } else {
                        long start_number = Long.parseLong(String.valueOf(startNumber.getText()));
                        int phone_number = Integer.parseInt(String.valueOf(number.getText()));
                        generatorVCard(start_number, phone_number);
                    }
                }
            }
        });
    }

    /**
     * 获取存储权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == -1) {
                    this.finish();
                    Toast.makeText(this, "未获取存储权限", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 生成vcard文件
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SimpleDateFormat")
    public void generatorVCard(long number, int creatNumber) {


        //获取存储权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

        String fileName = "数量" + creatNumber + ".vcf";
        OutputStreamWriter writer;
        File file = new File(VCARD_FILE_PATH, fileName);
        //得到存储卡的根路径，将fileName的文件写入到根目录下
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            //通过循环遍历生成联系人，可以通过随机生成名字
            for (int i = 0; i <= creatNumber; i++) {
                number++;
                Log.e("number", String.valueOf(number));
                //创建一个联系人
                VCardComposer composer = new VCardComposer();
                ContactStruct contact1 = new ContactStruct();
                contact1.name = number + "";//名字
                contact1.addPhone(Contacts.Phones.TYPE_MOBILE, String.valueOf(number), null, true);
                String vcardString;
                vcardString = composer.createVCard(contact1, VCardComposer.VERSION_VCARD30_INT);
                //将vcardString写入
                writer.write(vcardString);
                writer.write("\n");
            }

            writer.close();
            Toast.makeText(MainActivity.this, "已成功导出到Download/" + fileName + "文件", Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (VCardException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    //跳转到GitHub
    public void goToGitHub(View view) {
        Uri uri = Uri.parse("https://github.com/zhao-pf/VcfCreate");
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);
        startActivity(intent);
    }
}
