package de.ait.secondlife.domain.dto;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;


@Data
@Schema(description = "Image's path dto")
@AllArgsConstructor
public class ImagePathsResponseDto {
    @Parameter(description = "Code of type of entity",
            examples = {
                    @ExampleObject(
                            name = "1",
                            value = "{\"1024x1024\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\1\\1024x1024_d3f1a2b3-c456-789d-012e-3456789abcde.jpg\"," +
                                    " \"320x320\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\1\\320x320_d3f1a2b3-c456-789d-012e-3456789abcde.jpg\"," +
                                    " \"64x64\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\1\\64x64_d3f1a2b3-c456-789d-012e-3456789abcde.jpg\"}"),
                    @ExampleObject(
                            name = "2",
                            value = "{\"1024x1024\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\2\\1024x1024_a1b2c3d4-e5f6-7890-1234-56789abcdef0.jpg\"," +
                                    " \"320x320\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\2\\320x320_a1b2c3d4-e5f6-7890-1234-56789abcdef0.jpg\", " +
                                    "\"64x64\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\2\\64x64_a1b2c3d4-e5f6-7890-1234-56789abcdef0.jpg\"}"),
                    @ExampleObject(
                            name = "3",
                            value = "{\"1024x1024\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\3\\1024x1024_0fedcba9-8765-4321-0fed-cba987654321.jpg\"," +
                                    " \"320x320\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\3\\320x320_0fedcba9-8765-4321-0fed-cba987654321.jpg\", " +
                                    "\"64x64\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\3\\64x64_0fedcba9-8765-4321-0fed-cba987654321.jpg\"}"),
                    @ExampleObject(
                            name = "4",
                            value = "{\"1024x1024\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\4\\1024x1024_12345678-9abc-def0-1234-56789abcdef0.jpg\", " +
                                    "\"320x320\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\4\\320x320_12345678-9abc-def0-1234-56789abcdef0.jpg\", " +
                                    "\"64x64\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\4\\64x64_12345678-9abc-def0-1234-56789abcdef0.jpg\"}"),
                    @ExampleObject(
                            name = "5",
                            value = "{\"1024x1024\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\5\\1024x1024_abcdef12-3456-7890-abcd-ef1234567890.jpg\", " +
                                    "\"320x320\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\5\\320x320_abcdef12-3456-7890-abcd-ef1234567890.jpg\"," +
                                    " \"64x64\": \"https://second-life.fra1.digitaloceanspaces.com\\prod\\offer\\5\\64x64_abcdef12-3456-7890-abcd-ef1234567890.jpg\"}")
            })
    private Map<Integer, Map<String, String>> images;
}
