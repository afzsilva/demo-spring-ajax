package br.com.mballem.demoajax.web.dwr;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.directwebremoting.Browser;
import org.directwebremoting.ScriptSessions;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.com.mballem.demoajax.repository.PromocaoRepository;

@Component
@RemoteProxy
public class DWRAlertaPromocoes {
	
	@Autowired
	private PromocaoRepository repository;
	
	private Timer timer;
	
	/*Retorna a data de promocao mais recente*/
	private LocalDateTime getCadastroByUltimaPromocao() {	
		
		//Sort.Direction sort = Sort.Direction.DESC;		
		//PageRequest pageRequest = PageRequest.of(0, 1, sort,"dataCadastro");
		PageRequest pageRequest = PageRequest.of(0, 1, Sort.by("dataCadastro").descending());
		System.out.println(pageRequest.toString());
		System.out.println(repository.findUltimaDataDePromocao(pageRequest).getContent().toString());
		LocalDateTime dataRetornada = repository.findUltimaDataDePromocao(pageRequest).getContent().get(0); 
		
		return dataRetornada;
	}
	
	
	@RemoteMethod
	public synchronized void init() {
		System.out.println("DWR ativado ...!!!");
		
		LocalDateTime lastDate = getCadastroByUltimaPromocao();
		
		WebContext context = WebContextFactory.get();
		
		timer = new Timer();
		timer.schedule(new AlertTask(context, lastDate), 10000, 60000);
	}
	
	//InnerClass
	class AlertTask extends TimerTask{
		
		private LocalDateTime lastDate;
		private WebContext context;
		private long count;

		public AlertTask(WebContext context,LocalDateTime lastDate) {
			this.lastDate = lastDate;
			this.context = context;
			
		}
		
		
		@Override
		public void run() {
			String session = context.getScriptSession().getId();
			Browser.withSession(context, session, new Runnable() {
				
				@Override
				public void run() {//run da thread
					// TODO Auto-generated method stub
					Map<String, Object> map = repository.totalAndUltimaDataDePromocaoByDataCadastro(lastDate);
					count = (Long) map.get("count");
					
					lastDate = map.get("lastDate") == null ? lastDate:(LocalDateTime) map.get("lastDate");
					
					//dados apenas para teste
					Calendar time = Calendar.getInstance();
					time.setTimeInMillis(context.getScriptSession().getLastAccessedTime());
					System.out.println("Count "+count+" lastDate "+lastDate+" < "+session+" > "+" < "+time.getTime()+" >");
					
					if (count > 0) {
						ScriptSessions.addFunctionCall("showButton", count);
					}
					
				}
			});		
			
		}
		
	}
	
	
}
