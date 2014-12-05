package ru.oxbao.sensor_test;


import android.content.Intent;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TestExecutorActivity extends ActionBarActivity {
    private RadioGroup m_radioGroupTests;
    private Button m_btnStartTest;
    private Button m_btnBack;
    private TextView m_textViewX;
    private TextView m_textViewY;
    private TextView m_textViewZ;
    private Spinner m_spinnerFiles;
    private LinearLayout m_linearLayoutTestExecutor;
    private TestExecutor m_testExecutor;
    private RadioButton m_radioTest1;
    private ProgressWheel m_progressBarWheel;
    final String LOG_TAG = "TestExecutorActivity";
    private Handler m_handler;
    private boolean m_popupIsStarted = false;
    public static boolean g_startTestFlag = false;
    private boolean m_isHomeButton = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_executor_test);
        Log.d(LOG_TAG, "CREATE");

        m_radioGroupTests = (RadioGroup) findViewById(R.id.radGrTests);
        m_btnStartTest = (Button) findViewById(R.id.btnStartTest);
        m_btnBack = (Button) findViewById(R.id.btnBack);
        m_textViewX = (TextView) findViewById(R.id.tvX);
        m_textViewY = (TextView) findViewById(R.id.tvY);
        m_textViewZ = (TextView) findViewById(R.id.tvZ);
     //   m_linearLayoutTestExecutor = (LinearLayout) findViewById(R.id.testLay);
        m_spinnerFiles = (Spinner) findViewById(R.id.spinnerFiles);
        m_radioTest1 = (RadioButton) findViewById(R.id.test1);
        m_progressBarWheel = (ProgressWheel) findViewById(R.id.progressBarWheel);
        m_testExecutor = new TestExecutor(this);

        getSupportActionBar().hide();
        InputDataActivity.g_flagEraseData = true; // Стереть введенную информацию. Снимет этот флаг кнопка назад или системнаяя назад

        /***********Запрет на зысыпание*****************/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        m_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                m_progressBarWheel.setProgress(msg.getData().getInt("count"));
            }
        };

        m_btnStartTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartTest();
            }
        });

        m_btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputDataActivity.g_flagEraseData = false;
                m_isHomeButton = false;
                m_testExecutor.Stop();
                finish();
            }
        });

        m_radioGroupTests.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.test1:
                        m_spinnerFiles.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.test2:
                        if (m_testExecutor.GetFilesNames() != null) {
                            m_spinnerFiles.setVisibility(View.VISIBLE);
                            m_spinnerFiles.setAdapter(m_testExecutor.GetFilesNames());
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.filesNotFound), Toast.LENGTH_SHORT).show();
                            m_radioTest1.setChecked(true);
                        }
                        break;
                }
            }
        });


    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "Resume");
        SetProgressBar(0);
        /***Ready for test*/
        if (m_popupIsStarted) {
            SetButtonEnabled(true);
            m_popupIsStarted = false;
        }
        if (g_startTestFlag){
            StartTest();
            g_startTestFlag = false;
        }
        m_isHomeButton = true;
        super.onResume();
    }


    public void SetProgressBar(int progress) {
        m_progressBarWheel.setProgress(progress);
    }

    public void SetMaxProgressBar(int max) {

        m_progressBarWheel.setBarLength(max);
    }

    public void OnTestFinished(Solutions.ResultTestEnum resultTestEnum) {

    }

    public void SetTextViews(double x, double y, double z) {
        m_textViewX.setText(String.valueOf(x));
        m_textViewY.setText(String.valueOf(y));
        m_textViewZ.setText(String.valueOf(z));
    }

    public String GetCheckedSpinner() {
        String result = "";
        try {
            result = m_spinnerFiles.getSelectedItem().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void SendMessage(int count) {
        Bundle bundle = new Bundle();
        bundle.putInt("count", count);
        Message message = new Message();
        message.setData(bundle);
        m_handler.sendMessage(message);
    }

    // При чтении с датчика возникают события новых значений после остановки.
    // Иногда открывается несколько результатов
    //Что б не открвывалось больше одного результата введен флаг.
    public void ShowResult() {
        if (!m_popupIsStarted) {
            Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
            intent.putExtra("TestDataValue", m_testExecutor.g_testData.toString());
         //   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            m_popupIsStarted = true;
            startActivity(intent);
        }
    }

    private void SetButtonEnabled(boolean flag) {
        if (flag) {
            m_btnStartTest.setBackgroundResource(R.drawable.oval_button_ready);
            m_btnStartTest.setText(getResources().getString(R.string.startTest));
            m_btnStartTest.setEnabled(true);
            m_btnStartTest.setTextColor(getResources().getColor(R.color.btnStartReadyTextColor));
        } else {
            m_btnStartTest.setText(getResources().getString(R.string.wait));
            m_btnStartTest.setBackgroundResource(R.drawable.oval_button_execute);
            m_btnStartTest.setTextColor(getResources().getColor(R.color.btnExecuteTextColor));
            m_btnStartTest.setEnabled(false);
        }
    }

    public void StartTest() {
        switch (m_radioGroupTests.getCheckedRadioButtonId()) {
            case R.id.test1:
                m_testExecutor.Start(TestExecutor.TestEnum.test1);
                break;
            case R.id.test2:
                m_testExecutor.Start(TestExecutor.TestEnum.test2);
                break;
        }

        SetButtonEnabled(false);
    }

    @Override
    protected void finalize() throws Throwable {
        m_testExecutor.Stop();
        Log.d(LOG_TAG, "FINALIZE");
        super.finalize();
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            Log.d(LOG_TAG, "Back");
            InputDataActivity.g_flagEraseData = false;
            m_isHomeButton = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        if (m_isHomeButton){
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        super.onPause();
    }
}
