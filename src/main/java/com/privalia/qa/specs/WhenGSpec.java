/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.privalia.qa.specs;

import com.csvreader.CsvReader;
import com.ning.http.client.Response;
import com.privalia.qa.cucumber.converter.ArrayListConverter;
import com.privalia.qa.utils.ThreadProperty;
import com.privalia.qa.cucumber.converter.NullableStringConverter;
import cucumber.api.DataTable;
import cucumber.api.Transform;
import cucumber.api.java.en.When;
import org.hjson.JsonArray;
import org.hjson.JsonValue;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.io.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.privalia.qa.assertions.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Generic When Specs.
 * @see <a href="WhenGSpec-annotations.html">When Steps &amp; Matching Regex</a>
 */
public class WhenGSpec extends BaseGSpec {

    public static final int DEFAULT_TIMEOUT = 1000;

    /**
     * Default constructor.
     *
     * @param spec
     */
    public WhenGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Wait seconds.
     *
     * @param seconds
     * @throws InterruptedException
     */
    @When("^I wait '(.+?)' seconds?$")
    public void idleWait(Integer seconds) throws InterruptedException {
        Thread.sleep(seconds * DEFAULT_TIMEOUT);
    }

    /**
     * Searchs for two webelements dragging the first one to the second
     *
     * @param source
     * @param destination
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    @When("^I drag '([^:]*?):(.+?)' and drop it to '([^:]*?):(.+?)'$")
    public void seleniumDrag(String smethod, String source, String dmethod, String destination) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Actions builder = new Actions(commonspec.getDriver());

        List<WebElement> sourceElement = commonspec.locateElement(smethod, source, 1);
        List<WebElement> destinationElement = commonspec.locateElement(dmethod, destination, 1);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }

    /**
     * Click on an numbered {@code url} previously found element.
     *
     * @param index
     * @throws InterruptedException
     */
    @When("^I click on the element on index '(\\d+?)'$")
    public void seleniumClick(Integer index) throws InterruptedException {

        try {
            assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                    .hasAtLeast(index);
            commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();
        } catch (AssertionError e) {
            Thread.sleep(1000);
            assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                    .hasAtLeast(index);
            commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();
        }
    }

    /**
     * Clear the text on a numbered {@code index} previously found element.
     *
     * @param index
     */
    @When("^I clear the content on text input at index '(\\d+?)'$")
    public void seleniumClear(Integer index) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);

        assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).isTextField(commonspec.getTextFieldCondition());

        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).clear();
    }

    /**
     * Type a {@code text} on an numbered {@code index} previously found element.
     *
     * @param text
     * @param index
     */
    @When("^I type '(.+?)' on the element on index '(\\d+?)'$")
    public void seleniumType(@Transform(NullableStringConverter.class) String text, Integer index) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        while (text.length() > 0) {
            if (-1 == text.indexOf("\\n")) {
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(text);
                text = "";
            } else {
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(text.substring(0, text.indexOf("\\n")));
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(Keys.ENTER);
                text = text.substring(text.indexOf("\\n") + 2);
            }
        }
    }

    /**
     * Send a {@code strokes} list on an numbered {@code url} previously found element or to the driver. strokes examples are "HOME, END"
     * or "END, SHIFT + HOME, DELETE". Each element in the stroke list has to be an element from
     * {@link org.openqa.selenium.Keys} (NULL, CANCEL, HELP, BACK_SPACE, TAB, CLEAR, RETURN, ENTER, SHIFT, LEFT_SHIFT,
     * CONTROL, LEFT_CONTROL, ALT, LEFT_ALT, PAUSE, ESCAPE, SPACE, PAGE_UP, PAGE_DOWN, END, HOME, LEFT, ARROW_LEFT, UP,
     * ARROW_UP, RIGHT, ARROW_RIGHT, DOWN, ARROW_DOWN, INSERT, DELETE, SEMICOLON, EQUALS, NUMPAD0, NUMPAD1, NUMPAD2,
     * NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9, MULTIPLY, ADD, SEPARATOR, SUBTRACT, DECIMAL,
     * DIVIDE, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, META, COMMAND, ZENKAKU_HANKAKU) , a plus sign (+), a
     * comma (,) or spaces ( )
     *
     * @param strokes
     * @param foo
     * @param index
     */
    @When("^I send '(.+?)'( on the element on index '(\\d+?)')?$")
    public void seleniumKeys(@Transform(ArrayListConverter.class) List<String> strokes, String foo, Integer index) {
        if (index != null) {
            assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                    .hasAtLeast(index);
        }
        assertThat(strokes).isNotEmpty();

        for (String stroke : strokes) {
            if (stroke.contains("+")) {
                List<Keys> csl = new ArrayList<Keys>();
                for (String strokeInChord : stroke.split("\\+")) {
                    csl.add(Keys.valueOf(strokeInChord.trim()));
                }
                Keys[] csa = csl.toArray(new Keys[csl.size()]);
                if (index == null) {
                    new Actions(commonspec.getDriver()).sendKeys(commonspec.getDriver().findElement(By.tagName("body")), csa).perform();
                } else {
                    commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(csa);
                }
            } else {
                if (index == null) {
                    new Actions(commonspec.getDriver()).sendKeys(commonspec.getDriver().findElement(By.tagName("body")), Keys.valueOf(stroke)).perform();
                } else {
                    commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(Keys.valueOf(stroke));
                }
            }
        }
    }

    /**
     * Choose an @{code option} from a select webelement found previously
     *
     * @param option
     * @param index
     */
    @When("^I select '(.+?)' on the element on index '(\\d+?)'$")
    public void elementSelect(String option, Integer index) {
        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        sel.selectByVisibleText(option);
    }

    /**
     * Choose no option from a select webelement found previously
     *
     * @param index
     */
    @When("^I de-select every item on the element on index '(\\d+?)'$")
    public void elementDeSelect(Integer index) {
        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        if (sel.isMultiple()) {
            sel.deselectAll();
        }
    }

    /**
     * Send a request of the type specified
     *
     * @param requestType   type of request to be sent. Possible values:
     *                      GET|DELETE|POST|PUT|CONNECT|PATCH|HEAD|OPTIONS|REQUEST|TRACE
     * @param endPoint      end point to be used
     * @param foo           parameter generated by cucumber because of the optional expression
     * @param baseData      path to file containing the schema to be used
     * @param type          element to read from file (element should contain a json)
     * @param modifications DataTable containing the modifications to be done to the
     *                      base schema element. Syntax will be:
     *                      {@code
     *                      | <key path> | <type of modification> | <new value> |
     *                      }
     *                      where:
     *                      key path: path to the key to be modified
     *                      type of modification: DELETE|ADD|UPDATE
     *                      new value: in case of UPDATE or ADD, new value to be used
     *                      for example:
     *                      if the element read is {"key1": "value1", "key2": {"key3": "value3"}}
     *                      and we want to modify the value in "key3" with "new value3"
     *                      the modification will be:
     *                      | key2.key3 | UPDATE | "new value3" |
     *                      being the result of the modification: {"key1": "value1", "key2": {"key3": "new value3"}}
     * @throws Exception
     */
    @When("^I send a '(.+?)' request to '(.+?)'( with user and password '(.+:.+?)')? based on '([^:]+?)'( as '(json|string)')? with:.$")
    public void sendRequest(String requestType, String endPoint, String foo, String loginInfo, String baseData, String baz, String type, DataTable modifications) throws Exception {
        // Retrieve data
        String retrievedData = commonspec.retrieveData(baseData, type);

        // Modify data
        commonspec.getLogger().debug("Modifying data {} as {}", retrievedData, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();

        String user = null;
        String password = null;
        if (loginInfo != null) {
            user = loginInfo.substring(0, loginInfo.indexOf(':'));
            password = loginInfo.substring(loginInfo.indexOf(':') + 1, loginInfo.length());
        }


        commonspec.getLogger().debug("Generating request {} to {} with data {} as {}", requestType, endPoint, modifiedData, type);
        Future<Response> response = commonspec.generateRequest(requestType, false, user, password, endPoint, modifiedData, type, "");

        // Save response
        commonspec.getLogger().debug("Saving response");

        try {
            Response r = response.get();
            commonspec.setResponse(requestType, response.get());
        } catch (Exception e) {
            fail("Unable to get a response from the remote server", e);
        }

    }

    /**
     * Same sendRequest, but in this case, we do not receive a data table with modifications.
     * Besides, the data and request header are optional as well.
     * In case we want to simulate sending a json request with empty data, we just to avoid baseData
     *
     * @param requestType
     * @param endPoint
     * @param foo
     * @param baseData
     * @param bar
     * @param type
     * @throws Exception
     */
    @When("^I send a '(.+?)' request to '(.+?)'( with user and password '(.+:.+?)')?( based on '([^:]+?)')?( as '(json|string)')?.$")
    public void sendRequestNoDataTable(String requestType, String endPoint, String foo, String loginInfo, String bar, String baseData, String baz, String type) throws Exception {
        Future<Response> response;
        String user = null;
        String password = null;

        if (loginInfo != null) {
            user = loginInfo.substring(0, loginInfo.indexOf(':'));
            password = loginInfo.substring(loginInfo.indexOf(':') + 1, loginInfo.length());
        }

        if (baseData != null) {
            // Retrieve data
            String retrievedData = commonspec.retrieveData(baseData, type);
            // Generate request
            response = commonspec.generateRequest(requestType, false, user, password, endPoint, retrievedData, type, "");
        } else {
            // Generate request
            response = commonspec.generateRequest(requestType, false, user, password, endPoint, "", type, "");
        }

        // Save response
        commonspec.getLogger().debug("Saving response");

        try {
            Response r = response.get();
            commonspec.setResponse(requestType, response.get());
        } catch (Exception e) {
            fail("Unable to get a response from the remote server", e);
        }

    }

    /**
     * Same sendRequest, but in this case, the rersponse is checked until it contains the expected value
     *
     * @param timeout
     * @param wait
     * @param requestType
     * @param endPoint
     * @param responseVal
     * @throws Exception
     */
    @When("^in less than '(\\d+?)' seconds, checking each '(\\d+?)' seconds, I send a '(.+?)' request to '(.+?)' so that the response( does not)? contains '(.+?)'.$")
    public void sendRequestTimeout(Integer timeout, Integer wait, String requestType, String endPoint, String contains, String responseVal) throws Exception {

        Boolean searchUntilContains;
        if (contains == null || contains.isEmpty()) {
            searchUntilContains = Boolean.TRUE;
        } else {
            searchUntilContains = Boolean.FALSE;
        }
        Boolean found = !searchUntilContains;
        AssertionError ex = null;

        String type = "";
        Future<Response> response;
        Pattern pattern = CommonG.matchesOrContains(responseVal);
        for (int i = 0; (i <= timeout); i += wait) {
            if (found && searchUntilContains) {
                break;
            }
            response = commonspec.generateRequest(requestType, false, null, null, endPoint, "", type, "");
            commonspec.setResponse(requestType, response.get());
            commonspec.getLogger().debug("Checking response value");
            try {
                if (searchUntilContains) {
                    assertThat(commonspec.getResponse().getResponse()).containsPattern(pattern);
                    found = true;
                    timeout = i;
                } else {
                    assertThat(commonspec.getResponse().getResponse()).doesNotContain(responseVal);
                    found = false;
                    timeout = i;
                }
            } catch (AssertionError e) {
                if (!found) {
                    commonspec.getLogger().info("Response value not found after " + i + " seconds");
                } else {
                    commonspec.getLogger().info("Response value found after " + i + " seconds");
                }
                Thread.sleep(wait * 1000);
                ex = e;
            }
            if (!found && !searchUntilContains) {
                break;
            }
        }
        if ((!found && searchUntilContains) || (found && !searchUntilContains)) {
            throw (ex);
        }
        if (searchUntilContains) {
            commonspec.getLogger().info("Success! Response value found after " + timeout + " seconds");
        } else {
            commonspec.getLogger().info("Success! Response value not found after " + timeout + " seconds");
        }
    }

    @When("^I login to '(.+?)' based on '([^:]+?)' as '(json|string)'$")
    public void loginUser(String endPoint, String baseData, String type) throws Exception {
        sendRequestNoDataTable("POST", endPoint, null, null, null, baseData, null, type);
    }

    @When("^I login to '(.+?)' based on '([^:]+?)' as '(json|string)' with:$")
    public void loginUser(String endPoint, String baseData, String type, DataTable modifications) throws Exception {
        sendRequest("POST", endPoint, null, null, baseData, "", type, modifications);
    }

    @When("^I logout from '(.+?)'$")
    public void logoutUser(String endPoint) throws Exception {
        sendRequestNoDataTable("GET", endPoint, null, null, null, "", null, "");
    }

    /**
     * Read csv file and store result in list of maps
     *
     * @param csvFile
     */
    @When("^I read info from csv file '(.+?)'$")
    public void readFromCSV(String csvFile) throws Exception {
        CsvReader rows = new CsvReader(csvFile);

        String[] columns = null;
        if (rows.readRecord()) {
            columns = rows.getValues();
            rows.setHeaders(columns);
        }

        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        while (rows.readRecord()) {
            Map<String, String> row = new HashMap<String, String>();
            for (String column : columns) {
                row.put(column, rows.get(rows.getIndex(column)));
            }
            results.add(row);
        }

        rows.close();

        commonspec.setResultsType("csv");
        commonspec.setCSVResults(results);
    }

    /**
     * Change current window to another opened window.
     */
    @When("^I change active window$")
    public void seleniumChangeWindow() {
        String originalWindowHandle = commonspec.getDriver().getWindowHandle();
        Set<String> windowHandles = commonspec.getDriver().getWindowHandles();

        for (String window : windowHandles) {
            if (!window.equals(originalWindowHandle)) {
                commonspec.getDriver().switchTo().window(window);
            }
        }

    }

    /**
     * Sort elements in envVar by a criteria and order.
     *
     * @param envVar   Environment variable to be sorted
     * @param criteria alphabetical,...
     * @param order    ascending or descending
     */
    @When("^I sort elements in '(.+?)' by '(.+?)' criteria in '(.+?)' order$")
    public void sortElements(String envVar, String criteria, String order) {

        String value = ThreadProperty.get(envVar);
        JsonArray jsonArr = JsonValue.readHjson(value).asArray();

        List<JsonValue> jsonValues = new ArrayList<JsonValue>();
        for (int i = 0; i < jsonArr.size(); i++) {
            jsonValues.add(jsonArr.get(i));
        }

        Comparator<JsonValue> comparator;
        switch (criteria) {
            case "alphabetical":
                commonspec.getLogger().debug("Alphabetical criteria selected.");
                comparator = new Comparator<JsonValue>() {
                    public int compare(JsonValue json1, JsonValue json2) {
                        int res = String.CASE_INSENSITIVE_ORDER.compare(json1.toString(), json2.toString());
                        if (res == 0) {
                            res = json1.toString().compareTo(json2.toString());
                        }
                        return res;
                    }
                };
                break;
            default:
                commonspec.getLogger().debug("No criteria selected.");
                comparator = null;
        }

        if ("ascending".equals(order)) {
            Collections.sort(jsonValues, comparator);
        } else {
            Collections.sort(jsonValues, comparator.reversed());
        }

        ThreadProperty.set(envVar, jsonValues.toString());
    }


    /**
     * Create a JSON in resources directory with given name, so for using it you've to reference it as:
     * $(pwd)/target/test-classes/fileName
     *
     * @param fileName      name of the JSON file to be created
     * @param baseData      path to file containing the schema to be used
     * @param type          element to read from file (element should contain a json)
     * @param modifications DataTable containing the modifications to be done to the base schema element
     *                      <p>
     *                      - Syntax will be:
     *                      {@code
     *                      | <key path> | <type of modification> | <new value> |
     *                      }
     *                      for DELETE/ADD/UPDATE/APPEND/PREPEND
     *                      where:
     *                      key path: path to the key to be modified
     *                      type of modification: DELETE/ADD/UPDATE/APPEND/PREPEND
     *                      new value: new value to be used
     *                      <p>
     *                      - Or:
     *                      {@code
     *                      | <key path> | <type of modification> | <new value> | <new value type> |
     *                      }
     *                      for REPLACE
     *                      where:
     *                      key path: path to the key to be modified
     *                      type of modification: REPLACE
     *                      new value: new value to be used
     *                      json value type: type of the json property (array|object|number|boolean|null|n/a (for string))
     *                      <p>
     *                      <p>
     *                      For example:
     *                      <p>
     *                      (1)
     *                      If the element read is {"key1": "value1", "key2": {"key3": "value3"}}
     *                      and we want to modify the value in "key3" with "new value3"
     *                      the modification will be:
     *                      | key2.key3 | UPDATE | "new value3" |
     *                      being the result of the modification: {"key1": "value1", "key2": {"key3": "new value3"}}
     *                      <p>
     *                      (2)
     *                      If the element read is {"key1": "value1", "key2": {"key3": "value3"}}
     *                      and we want to replace the value in "key2" with {"key4": "value4"}
     *                      the modification will be:
     *                      | key2 | REPLACE | {"key4": "value4"} | object |
     *                      being the result of the modification: {"key1": "value1", "key2": {"key4": "value4"}}
     * @throws Exception
     */
    @When("^I create file '(.+?)' based on '(.+?)' as '(.+?)' with:$")
    public void createFile(String fileName, String baseData, String type, DataTable modifications) throws Exception {
        // Retrieve data
        String retrievedData = commonspec.retrieveData(baseData, type);

        // Modify data
        commonspec.getLogger().debug("Modifying data {} as {}", retrievedData, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();

        // Create file (temporary) and set path to be accessible within test
        File tempDirectory = new File(String.valueOf(System.getProperty("user.dir") + "/target/test-classes/"));
        String absolutePathFile = tempDirectory.getAbsolutePath() + "/" + fileName;
        commonspec.getLogger().debug("Creating file {} in 'target/test-classes'", absolutePathFile);
        // Note that this Writer will delete the file if it exists
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(absolutePathFile), "UTF-8"));
        try {
            out.write(modifiedData);
        } catch (Exception e) {
            commonspec.getLogger().error("Custom file {} hasn't been created:\n{}", absolutePathFile, e.toString());
        } finally {
            out.close();
        }

        assertThat(new File(absolutePathFile).isFile());
    }

    /**
     * Read the file passed as parameter, perform the modifications specified and save the result in the environment
     * variable passed as parameter.
     * @param baseData      file to read
     * @param type          whether the info in the file is a 'json' or a simple 'string'
     * @param envVar        name of the variable where to store the result
     * @param modifications modifications to perform in the content of the file
     */
    @When("^I read file '(.+?)' as '(.+?)' and save it in environment variable '(.+?)' with:$")
    public void readFileToVariable(String baseData, String type, String envVar, DataTable modifications) throws Exception {
        // Retrieve data
        String retrievedData = commonspec.retrieveData(baseData, type);

        // Modify data
        commonspec.getLogger().debug("Modifying data {} as {}", retrievedData, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();

        // Save in environment variable
        ThreadProperty.set(envVar, modifiedData);
    }

    /**
     * Read the file passed as parameter and save the result in the environment
     * variable passed as parameter.
     * @param baseData      file to read
     * @param type          whether the info in the file is a 'json' or a simple 'string'
     * @param envVar        name of the variable where to store the result
     */
    @When("^I read file '(.+?)' as '(.+?)' and save it in environment variable '(.+?)'$")
    public void readFileToVariableNoDataTable(String baseData, String type, String envVar) throws Exception {
        // Retrieve data
        String retrievedData = commonspec.retrieveData(baseData, type);

        // Save in environment variable
        ThreadProperty.set(envVar, retrievedData);
    }

}
