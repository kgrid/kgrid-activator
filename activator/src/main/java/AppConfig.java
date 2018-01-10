import edu.umich.lhs.activator.services.KobjectConverter;
import java.util.List;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by grosscol on 2018-01-10.
 */


@ComponentScan
public class AppConfig extends WebMvcConfigurerAdapter {

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new KobjectConverter());
  }
}