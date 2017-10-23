package io.vertigo.dynamo.impl.task.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import io.vertigo.app.Home;
import io.vertigo.core.component.proxy.ProxyMethod;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.task.proxy.TaskInput;
import io.vertigo.dynamo.task.proxy.TaskOutput;
import io.vertigo.lang.Assertion;

public final class TaskProxyMethod implements ProxyMethod {

	@Override
	public Class<io.vertigo.dynamo.task.proxy.TaskAnnotation> getAnnotationType() {
		return io.vertigo.dynamo.task.proxy.TaskAnnotation.class;
	}

	private static Domain resolveDomain(final String domainName) {
		//is there an available domain on the parameter?
		// - yes
		//todo
		//		if (DtList.class.isAssignableFrom(type)) {
		//			return Domain.builder(
		//					"DO_L",
		//					DtObjectUtil.findDtDefinition(
		//							(Class<DtObject>) ClassUtil.getGeneric(type,
		//									() -> new RuntimeException("No generic found on the list")))
		//							.getName(),
		//					true).build();
		//		}
		//		//- no then we have to build one.
		//		final DataType dataType = DataType.of(type).get();
		//		return Domain.builder("DO_CC", dataType, false).build();
		return Home.getApp().getDefinitionSpace().resolve(domainName, Domain.class);
	}

	private static boolean hasOut(final Method method) {
		return !void.class.equals(method.getReturnType());
	}

	private static boolean isOutOptional(final Method method) {
		return Optional.class.isAssignableFrom(method.getReturnType());
	}

	private static Domain findOutDomain(final Method method) {
		final TaskOutput taskOutput = method.getAnnotation(TaskOutput.class);
		Assertion.checkNotNull(taskOutput, "The return method '{0}' must be annotated with '{1}'", method, TaskOutput.class);
		return resolveDomain(taskOutput.domain());
		//		//is there an available domain on the parameter?
		//		// - yes
		//		//todo
		//		if (List.class.isAssignableFrom(method.getReturnType())) {
		//			final Class returnClass = ClassUtil.getGeneric(
		//					method.getGenericReturnType(),
		//					() -> new RuntimeException("No generic found on the list"));
		//			//- no then we have to build one.
		//
		//			final Optional<DataType> dataTypeOpt = DataType.of(returnClass);
		//			if (dataTypeOpt.isPresent()) {
		//				return Domain.builder(
		//						"DO_OUT",
		//						dataTypeOpt.get(),
		//						true).build();
		//			}
		//			return Domain.builder(
		//					"DO_OUT",
		//					DtObjectUtil.findDtDefinition(returnClass).getName(),
		//					true).build();
		//		} else if (Optionnal.class.isAssignableFrom(method.getReturnType())) {
		//
		//		}
		//		//- no then we have to build one.
		//		final DataType dataType = DataType.of(method.getReturnType()).get();
		//		return Domain.builder("DO_OUT", dataType, false).build();
	}

	private static TaskManager getTaskManager() {
		return Home.getApp().getComponentSpace().resolve(TaskManager.class);
	}

	@Override
	public Object invoke(final Method method, final Object[] args) {
		final TaskDefinition taskDefinition = createTaskDefinition(method);
		final Task task = createTask(taskDefinition, method, args);
		final TaskResult taskResult = getTaskManager().execute(task);
		if (taskDefinition.getOutAttributeOption().isPresent()) {
			return taskResult.getResult();
		}
		return Void.TYPE;
	}

	private static TaskDefinition createTaskDefinition(final Method method) {
		final io.vertigo.dynamo.task.proxy.TaskAnnotation taskAnnotation = method.getAnnotation(io.vertigo.dynamo.task.proxy.TaskAnnotation.class);

		final TaskDefinitionBuilder taskDefinitionBuilder = TaskDefinition.builder(taskAnnotation.name())
				.withEngine(taskAnnotation.taskEngineClass())
				.withRequest(taskAnnotation.request())
				.withDataSpace(taskAnnotation.dataSpace().isEmpty() ? null : taskAnnotation.dataSpace());

		if (hasOut(method)) {
			final Domain outDomain = findOutDomain(method);
			if (isOutOptional(method)) {
				taskDefinitionBuilder.withOutOptional("OUT", outDomain);

			} else {
				taskDefinitionBuilder.withOutRequired("OUT", outDomain);
			}
		}
		for (final Parameter parameter : method.getParameters()) {
			final TaskInput taskAttributeAnnotation = parameter.getAnnotation(TaskInput.class);

			//test if the parameter is an optional type
			final boolean optional = Optional.class.isAssignableFrom(parameter.getType());

			if (optional) {
				taskDefinitionBuilder.addInOptional(
						taskAttributeAnnotation.name(),
						resolveDomain(taskAttributeAnnotation.domain()));
			} else {
				taskDefinitionBuilder.addInRequired(
						taskAttributeAnnotation.name(),
						resolveDomain(taskAttributeAnnotation.domain()));
			}
		}

		return taskDefinitionBuilder.build();
	}

	private static Task createTask(final TaskDefinition taskDefinition, final Method method, final Object[] args) {
		final TaskBuilder taskBuilder = Task.builder(taskDefinition);
		for (int i = 0; i < method.getParameters().length; i++) {
			final Parameter parameter = method.getParameters()[i];
			final boolean optional = Optional.class.isAssignableFrom(parameter.getType());
			final TaskInput taskAttributeAnnotation = parameter.getAnnotation(TaskInput.class);

			final Object arg;
			if (optional) {
				arg = ((Optional) (args[i])).orElse(null);
			} else {
				arg = args[i];
			}
			taskBuilder.addValue(taskAttributeAnnotation.name(), arg);
		}
		return taskBuilder.build();
	}
}
