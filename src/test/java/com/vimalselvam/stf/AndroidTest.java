package com.vimalselvam.stf;

import com.vimalselvam.DeviceApi;
import com.vimalselvam.STFService;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.AppiumCommandExecutor;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Scanner;

public class AndroidTest {
    private static final String STF_SERVICE_URL = "http://188.138.211.34:7100";  // Cristi URL
    //    private static final String STF_SERVICE_URL = "http://192.168.100.38:7100";  // Sergiu  URL
    private static final String ACCESS_TOKEN = "007c443d863041eaa9ddde3dfdaeee7369d1917bd91941f099db3830ab87b6eb";  // Cristi token
//    private static final String ACCESS_TOKEN = "711fdae9975a40bab789c6e27146dc32bf5a04140ada44d98854484ad7156f41";  // Sergiu token

    private AndroidDriver androidDriver;
    private String deviceSerial;
    private String remoteUrl;
    private AppiumDriverLocalService service;
    private DeviceApi deviceApi;

    @Factory(dataProvider = "parallelDp")
    public AndroidTest(String deviceSerial) {
        if(!deviceSerial.equalsIgnoreCase("Default test name")) {
        this.deviceSerial = deviceSerial.substring(0, deviceSerial.indexOf(","));
        this.remoteUrl = deviceSerial.substring(deviceSerial.indexOf(",") + 1);
        }
    }

    private void createAppiumService() {
        this.service = AppiumDriverLocalService.buildDefaultService();
//                .buildService(new AppiumServiceBuilder()
//                        .withIPAddress("0.0.0.0")
//                        .usingPort(4723));

        this.service.start();
    }

    private void connectToStfDevice() throws MalformedURLException, URISyntaxException, InterruptedException {
        STFService stfService = new STFService(STF_SERVICE_URL,
                ACCESS_TOKEN);
        this.deviceApi = new DeviceApi(stfService);
        this.deviceApi.connectDevice(this.deviceSerial, this.remoteUrl);
    }

    @BeforeClass
    public void setup() throws IOException, URISyntaxException, InterruptedException {

//        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir").concat("/src/driver/chromedriver.exe"));
//        WebDriver driver = new ChromeDriver();
//        driver.get("http://188.138.211.34:7100");
//        driver.findElement(By.name("username")).sendKeys("administrator");
//        driver.findElement(By.name("email")).sendKeys("administrator@fakedomain.com");
//        driver.findElement(By.xpath("//input[@class='btn btn-lg btn-primary btn-block']")).click();
//        driver.get("http://188.138.211.34:7100/#!/control/4f74d62f");
//        driver.quit();

        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android");
        desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "ANDROID");
        desiredCapabilities.setCapability("deviceId", this.deviceSerial);
//        desiredCapabilities.setCapability("remoteAdbHost", this.remoteUrl);
        desiredCapabilities.setCapability(MobileCapabilityType.APP,
                new File("src/main/resources/android/release/1.12.0.apk").getAbsolutePath());



        connectToStfDevice();
        createAppiumService();
//        executeCmd("adb connect " + remoteUrl); // SM - A1
//        executeCmd("adb connect 188.138.211.34:7649"); // Redmi Note 6 Pro
//        executeCmd("adb connect 192.168.100.38:7417");  // Sergiu device Moto
//        executeCmd("adb connect 192.168.100.38:7433"); // My device Samsung
//        executeCmd("adb connect 188.138.211.34:7665"); // SM - j1
//        executeCmd("adb connect 188.138.211.34:7541"); // Redmi note 7


        try {
            androidDriver = new AndroidDriver(this.service.getUrl(), desiredCapabilities);
        } catch (Exception e) {
            tearDown();
        }
    }

    @Test
    public void currentActivityTest() {
        Assert.assertEquals(androidDriver.currentActivity(), ".app.screens.main.MainActivity", "Activity not match");
    }

    @Test(dependsOnMethods = {"currentActivityTest"})
    public void scrollingToSubElement() throws InterruptedException {
        Thread.sleep(10000);
        String holidayTitle = androidDriver.findElementById("ru.otkritkiok.pozdravleniya:id/error_page_message_text").getText();

        Assert.assertNotNull(holidayTitle);
    }

    @AfterClass
    public void tearDown() {
        if (androidDriver != null) {
            androidDriver.quit();
        }

        if (this.service.isRunning()) {
            service.stop();
            this.deviceApi.releaseDevice(this.deviceSerial);
        }
    }

    @DataProvider
    public Object[][] parallelDp() {
        return new Object[][]{
                {"4f74d62f,188.138.211.34:7453"}, // Redmi note 6 pro
//                {"b16afe4d"}, // My
//                {"ZY322BRFKR"}, // Sergiu
//                {"42005324a8bc6400"}, // SM - j1
//                {"4ad86c7,188.138.211.34:7665"}, // Redmi note 7 pro
                {"RZ8M74LXP9M,188.138.211.34:7605"}, // SM - A1

        };
    }

    /**
     * Execute terminal command
     *
     * @param command to execute
     * @return String output executed command
     * @throws IOException
     */
    public static String executeCmd(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        Scanner kb = new Scanner(process.getInputStream());

        return kb.next();
    }

}
