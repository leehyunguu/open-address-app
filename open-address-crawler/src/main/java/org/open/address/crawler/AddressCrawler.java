package org.open.address.crawler;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.open.address.domain.Address;
import org.open.address.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class AddressCrawler {

    @Value("${address.url}")
    String addressUrl;

    private final AddressRepository addressRepository;

    public AddressCrawler(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public void run(String type) throws Exception {

        File file = downloadZip(type);

        List<File> unzipFileList = unzip(file, file.getParentFile());

        insertToDB(unzipFileList);

    }

    private void insertToDB(List<File> unzipFileList) throws IOException {
        List<Address> AddressList = new ArrayList<>();
        for (File file : unzipFileList) {
            Files.lines(file.toPath(), Charset.forName("cp949")).forEach(string -> {
                if (!StringUtils.isEmpty(string)) {
                    String[] stringArray = string.split("\\|");

                    Address Address = new Address(stringArray[0], stringArray[1], stringArray[2], stringArray[3], stringArray[4],
                            stringArray[5], stringArray[6], stringArray[7], stringArray[8], stringArray[9], stringArray[10],
                            stringArray[11], stringArray[12], stringArray[13], stringArray[14], stringArray[15],
                            stringArray[16], stringArray[17], stringArray[18], null, null);

                    AddressList.add(Address);
                    if (AddressList.size() == 10000) {
                        addressRepository.saveAll(AddressList);
                        AddressList.clear();
                    }
                }
            });
        }
    }

    /**
     * Zip 파일의 압축을 푼다.
     *
     * @param file             - 압축 풀 Zip 파일
     * @param targetDir           - 압축 푼 파일이 들어간 디렉토리
     * @return 압축 해제된 FileList
     * @throws Exception 예외
     */
    public static List<File> unzip(File file, File targetDir) throws Exception {

        ZipFile zipFile = new ZipFile(file, Charset.forName("cp949"));
        InputStream is = null;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry zentry;

        List<File> targetFileList = new ArrayList<>();

        try {

            while (entries.hasMoreElements()) {
                zentry = entries.nextElement();

                if (zentry.getName().endsWith("txt")) {
                    is = zipFile.getInputStream(zentry); // ZipInputStream
                    String fileNameToUnzip = zentry.getName();

                    File targetFile = new File(targetDir, fileNameToUnzip);

                    if (zentry.isDirectory()) {// Directory 인 경우
                        FileUtils.forceMkdir(targetFile); // 디렉토리 생성
                    } else { // File 인 경우
                        // parent Directory 생성
                        FileUtils.forceMkdirParent(targetFile);
                        unzipEntry(is, targetFile);
                        targetFileList.add(targetFile);
                    }
                    if (is != null) {
                        is.close();
                    }
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
            zipFile.close();
        }

        return targetFileList;
    }

    /**
     * Zip 파일의 한 개 엔트리의 압축을 푼다.
     *
     * @param is       - Input Stream
     * @param targetFile - 압축 풀린 파일의 경로
     * @return 압축 해제한 파일
     * @throws Exception 예외
     */
    protected static File unzipEntry(InputStream is, File targetFile) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[2048];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        }
        return targetFile;
    }

    /**
     * 크롤링을 통해 ZIP파일 형태의 공공 주소 데이터를 다운로드한다.
     *
     * @param type A: 전체, A 외: 변동분
     * @return 다운로드한 Zip 파일
     * @throws IOException IO 예외
     */
    private File downloadZip(String type) throws IOException {

        WebClient webClient = new WebClient();

        // JavaScript에서 다운로드 태그가 생성되므로 활성화해야함
        webClient.getOptions().setJavaScriptEnabled(true);

        // 초기화시 Ajax 요청이 있으므로 선언 필요
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        HtmlPage page = webClient.getPage(addressUrl);
        List<Object> downloadList;
        if (type.contentEquals("A")) {
            // 전체 데이터 다운로드 태그
            downloadList = page.getByXPath("//div[@id='monthGeoAllDownNum']/a");
        } else {
            // 월변동 데이터 다운로드 태그
            downloadList = page.getByXPath("//div[@id='monthGeoChangeDownNum']/a");
        }
        // 최신 데이터 다운로드
        HtmlAnchor htmlAnchor = (HtmlAnchor) downloadList.get(downloadList.size() - 1);
        htmlAnchor.click();
        webClient.waitForBackgroundJavaScript(1000);

        Page downloadPage = webClient.getCurrentWindow().getEnclosedPage();

        // 다운로드 위치 지정
        File destFile = new File(System.getProperty("java.io.tmpdir"), "temp.zip");
        try (InputStream contentAsStream = downloadPage.getWebResponse().getContentAsStream()) {
            try (OutputStream out = new FileOutputStream(destFile)) {
                IOUtils.copy(contentAsStream, out);
            }
        }

        webClient.close();
        System.out.println("Output written to " + destFile.getAbsolutePath());
        return destFile;
    }

}
