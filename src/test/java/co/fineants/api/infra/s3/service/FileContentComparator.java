package co.fineants.api.infra.s3.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.assertj.core.api.Assertions;

public class FileContentComparator {

	public void compare(String content, String goldFilePath) {
		try {
			BufferedReader lead = new BufferedReader(new StringReader(content));
			BufferedReader gold = new BufferedReader(new FileReader(goldFilePath));

			String line;
			while ((line = gold.readLine()) != null) {
				Assertions.assertThat(lead.readLine()).isEqualTo(line);
			}
			Assertions.assertThat(lead.readLine()).isNull();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void compare(File file, String goldFilePath) {
		try {
			BufferedReader lead = new BufferedReader(new FileReader(file));
			BufferedReader gold = new BufferedReader(new FileReader(goldFilePath));

			String line;
			while ((line = gold.readLine()) != null) {
				Assertions.assertThat(lead.readLine()).isEqualTo(line);
			}
			Assertions.assertThat(lead.readLine()).isNull();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
