package org.loed.framework.common.web.flux.codec;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.loed.framework.common.Result;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/1 1:26 下午
 */
public class JsonResultWrapperEncoder extends Jackson2JsonEncoder {
	private static final Map<MediaType, byte[]> STREAM_SEPARATORS;
	private static final byte[] NEWLINE_SEPARATOR = {'\n'};

	static {
		STREAM_SEPARATORS = new HashMap<>(4);
		STREAM_SEPARATORS.put(MediaType.APPLICATION_STREAM_JSON, NEWLINE_SEPARATOR);
		STREAM_SEPARATORS.put(MediaType.parseMediaType("application/stream+x-jackson-smile"), new byte[0]);
	}

	public JsonResultWrapperEncoder() {
	}

	public JsonResultWrapperEncoder(ObjectMapper mapper, MimeType... mimeTypes) {
		super(mapper, mimeTypes);
	}

	@Override
	public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory,
	                               ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

		Assert.notNull(inputStream, "'inputStream' must not be null");
		Assert.notNull(bufferFactory, "'bufferFactory' must not be null");
		Assert.notNull(elementType, "'elementType' must not be null");
		if (inputStream instanceof Mono) {
			ResolvableType wrappedType = ResolvableType.forClassWithGenerics(Result.class, elementType);
			return Mono.from(inputStream).map(value -> {
				if (value instanceof Result) {
					return value;
				}
				return new Result<>(value);
			}).defaultIfEmpty(new Result<>())
					.map(value -> encodeValue(value, bufferFactory, wrappedType, mimeType, hints))
					.flux();
		} else {
			//streaming json, not supported now
			byte[] separator = streamSeparator(mimeType);
			if (separator != null) { // streaming
				try {
					ObjectWriter writer = createObjectWriter(elementType, mimeType, hints);
					ByteArrayBuilder byteBuilder = new ByteArrayBuilder(writer.getFactory()._getBufferRecycler());
					JsonEncoding encoding = getJsonEncoding(mimeType);
					JsonGenerator generator = getObjectMapper().getFactory().createGenerator(byteBuilder, encoding);
					SequenceWriter sequenceWriter = writer.writeValues(generator);

					return Flux.from(inputStream)
							.map(value -> encodeStreamingValue(value, bufferFactory, hints, sequenceWriter, byteBuilder,
									separator));
				} catch (IOException ex) {
					return Flux.error(ex);
				}
			} else { // non-streaming
				ResolvableType listType = ResolvableType.forClassWithGenerics(List.class, elementType);
				ResolvableType wrappedListType = ResolvableType.forClassWithGenerics(Result.class, listType);
				return Flux.from(inputStream)
						.collectList()
						.map(Result::new)
						.defaultIfEmpty(new Result<>())
						.map(list -> encodeValue(list, bufferFactory, wrappedListType, mimeType, hints))
						.flux();
			}
		}
	}

	@Nullable
	private byte[] streamSeparator(@Nullable MimeType mimeType) {
		for (MediaType streamingMediaType : getStreamingMediaTypes()) {
			if (streamingMediaType.isCompatibleWith(mimeType)) {
				return STREAM_SEPARATORS.getOrDefault(streamingMediaType, NEWLINE_SEPARATOR);
			}
		}
		return null;
	}


	private ObjectWriter createObjectWriter(ResolvableType valueType, @Nullable MimeType mimeType,
	                                        @Nullable Map<String, Object> hints) {

		JavaType javaType = getJavaType(valueType.getType(), null);
		Class<?> jsonView = (hints != null ? (Class<?>) hints.get(Jackson2CodecSupport.JSON_VIEW_HINT) : null);
		ObjectWriter writer = (jsonView != null ?
				getObjectMapper().writerWithView(jsonView) : getObjectMapper().writer());

		if (javaType.isContainerType()) {
			writer = writer.forType(javaType);
		}

		return customizeWriter(writer, mimeType, valueType, hints);
	}

//	@Override
//	protected ObjectWriter customizeWriter(ObjectWriter writer, @Nullable MimeType mimeType,
//	                                       ResolvableType elementType, @Nullable Map<String, Object> hints) {
//		// use generic object writer
//		return  getObjectMapper().writer();
//	}

	private DataBuffer encodeStreamingValue(Object value, DataBufferFactory bufferFactory, @Nullable Map<String, Object> hints,
	                                        SequenceWriter sequenceWriter, ByteArrayBuilder byteArrayBuilder, byte[] separator) {

		logValue(hints, value);

		try {
			sequenceWriter.write(value);
			sequenceWriter.flush();
		} catch (InvalidDefinitionException ex) {
			throw new CodecException("Type definition error: " + ex.getType(), ex);
		} catch (JsonProcessingException ex) {
			throw new EncodingException("JSON encoding error: " + ex.getOriginalMessage(), ex);
		} catch (IOException ex) {
			throw new IllegalStateException("Unexpected I/O error while writing to byte array builder", ex);
		}

		byte[] bytes = byteArrayBuilder.toByteArray();
		byteArrayBuilder.reset();

		int offset;
		int length;
		if (bytes.length > 0 && bytes[0] == ' ') {
			// SequenceWriter writes an unnecessary space in between values
			offset = 1;
			length = bytes.length - 1;
		} else {
			offset = 0;
			length = bytes.length;
		}
		DataBuffer buffer = bufferFactory.allocateBuffer(length + separator.length);
		buffer.write(bytes, offset, length);
		buffer.write(separator);

		return buffer;
	}

	private void logValue(@Nullable Map<String, Object> hints, Object value) {
		if (!Hints.isLoggingSuppressed(hints)) {
			LogFormatUtils.traceDebug(logger, traceOn -> {
				String formatted = LogFormatUtils.formatValue(value, !traceOn);
				return Hints.getLogPrefix(hints) + "Encoding [" + formatted + "]";
			});
		}
	}
}
