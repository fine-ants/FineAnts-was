package co.fineants.api.infra.s3.service.imple;

import java.util.Collection;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;

public class AmazonS3WriteDividendService implements WriteDividendService {

	private final CsvFormatter<StockDividend> formatter;
	private final RemoteFileUploader fileUploader;
	private final String filePath;

	public AmazonS3WriteDividendService(
		CsvFormatter<StockDividend> formatter,
		RemoteFileUploader fileUploader,
		String filePath) {
		this.formatter = formatter;
		this.fileUploader = fileUploader;
		this.filePath = filePath;
	}

	@Override
	public void writeDividend(Collection<StockDividend> dividends) {
		writeDividend(dividends.toArray(StockDividend[]::new));
	}

	@Override
	public void writeDividend(StockDividend... dividends) {
		String content = formatter.format(dividends);
		fileUploader.upload(content, filePath);
	}

	@Override
	public void writeDividendTemp(StockDividendTemp... dividends) {
		String[] headers = {"tickerSymbol", "dividend", "recordDate", "paymentDate", "isDeleted"};
		CsvFormatter<StockDividendTemp> csvFormatter = new CsvFormatter<>(",", headers);
		String content = csvFormatter.format(dividends);
		fileUploader.upload(content, filePath);
	}
}
