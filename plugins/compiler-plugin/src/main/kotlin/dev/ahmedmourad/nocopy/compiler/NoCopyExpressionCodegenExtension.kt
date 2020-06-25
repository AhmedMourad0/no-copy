package dev.ahmedmourad.nocopy.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
import org.jetbrains.kotlin.codegen.JvmKotlinType
import org.jetbrains.kotlin.codegen.OwnerKind.ERASED_INLINE_CLASS
import org.jetbrains.kotlin.codegen.context.FieldOwnerContext
import org.jetbrains.kotlin.codegen.context.MethodContext
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.incremental.components.NoLookupLocation.WHEN_GET_ALL_DESCRIPTORS
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.substitutedUnderlyingType
import org.jetbrains.org.objectweb.asm.AnnotationVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class NoCopyExpressionCodegenExtension(
        private val messageCollector: MessageCollector
) : ExpressionCodegenExtension {

    override fun generateClassSyntheticParts(codegen: ImplementationBodyCodegen) {
        val targetClass = codegen.descriptor
        messageCollector.log("Reading ${targetClass.name}")

        if (!targetClass.isData) {
            return
        }

        if (!targetClass.hasLeastVisibleCopy()) {
            return
        }

        if (targetClass.constructors.findLeastVisible()?.visibility != Visibilities.INTERNAL) {
            return
        }

        val constructor = targetClass.constructors.firstOrNull { it.isPrimary } ?: return
        val properties = constructor.valueParameters
                .mapNotNull { codegen.bindingContext.get(BindingContext.VALUE_PARAMETER_AS_PROPERTY, it) }

        CopyGenerator(
                declaration = codegen.myClass as KtClassOrObject,
                classDescriptor = targetClass,
                classAsmType = codegen.typeMapper.mapType(targetClass),
                fieldOwnerContext = codegen.context,
                v = codegen.v,
                generationState = codegen.state
        ).generateCopyMethod(
                targetClass.findCopyFunction()!!,
                properties
        )
    }
}

private class CopyGenerator(
        private val declaration: KtClassOrObject,
        private val classDescriptor: ClassDescriptor,
        private val classAsmType: Type,
        private val fieldOwnerContext: FieldOwnerContext<*>,
        private val v: ClassBuilder,
        private val generationState: GenerationState
) {
    private val typeMapper: KotlinTypeMapper = generationState.typeMapper
    private val underlyingType: JvmKotlinType

    private val copyDesc: String
        get() = "($firstParameterDesc)Ljava/lang/String;"

    private val firstParameterDesc: String
        get() {
            return if (fieldOwnerContext.contextKind == ERASED_INLINE_CLASS) {
                underlyingType.type.descriptor
            } else {
                ""
            }
        }

    private val access: Int
        get() {
            var access = Opcodes.ACC_PUBLIC or Opcodes.ACC_FINAL
            if (fieldOwnerContext.contextKind == ERASED_INLINE_CLASS) {
                access = access or Opcodes.ACC_STATIC
            }

            return access
        }

    init {
        this.underlyingType = JvmKotlinType(
                typeMapper.mapType(classDescriptor),
                classDescriptor.defaultType.substitutedUnderlyingType()
        )
    }

    fun generateCopyMethod(function: FunctionDescriptor, properties: List<PropertyDescriptor>) {
//        val context = fieldOwnerContext.intoFunction(function)
//        val methodOrigin = OtherOrigin(function)
//        val copyMethodName = mapFunctionName(function) //TODO: might need tinkering
//        val mv = v.newMethod(methodOrigin, access, copyMethodName, copyDesc, null, null)
//
//        if (fieldOwnerContext.contextKind != ERASED_INLINE_CLASS && classDescriptor.isInline) {
//            FunctionCodegen.generateMethodInsideInlineClassWrapper(
//                    methodOrigin,
//                    function,
//                    classDescriptor,
//                    mv,
//                    typeMapper
//            )
//            return
//        }
//
//        visitEndForAnnotationVisitor(
//                mv.visitAnnotation(Type.getDescriptor(NotNull::class.java), false)
//        )
//
//        if (!generationState.classBuilderMode.generateBodies) {
//            FunctionCodegen.endVisit(mv, copyMethodName, declaration)
//            return
//        }
//
//        val iv = InstructionAdapter(mv)
//
//        mv.visitCode()
//        AsmUtil.genStringBuilderConstructor(iv)
//
//        if (properties.isEmpty()) {
//            // This is a redacted class, so just emit a single replacementString
//            iv.aconst(classDescriptor.name.toString() + "(" + replacementString)
//            AsmUtil.genInvokeAppendMethod(iv, AsmTypes.JAVA_STRING_TYPE, null)
//        } else {
//            var first = true
//            for (propertyDescriptor in properties) {
//                val isRedacted = propertyDescriptor.isRedacted(fqRedactedAnnotation)
//                val possibleValue = if (isRedacted) replacementString else ""
//                if (first) {
//                    iv.aconst(classDescriptor.name.toString() + "(" + propertyDescriptor.name
//                            .asString() + "=$possibleValue")
//                    first = false
//                } else {
//                    iv.aconst(", " + propertyDescriptor.name
//                            .asString() + "=$possibleValue")
//                }
//                AsmUtil.genInvokeAppendMethod(iv, AsmTypes.JAVA_STRING_TYPE, null)
//
//                if (!isRedacted) {
//                    val type = genOrLoadOnStack(iv, context, propertyDescriptor, 0)
//                    var asmType = type.type
//                    var kotlinType = type.kotlinType
//
//                    if (asmType.sort == Type.ARRAY) {
//                        val elementType = AsmUtil.correctElementType(asmType)
//                        if (elementType.sort == Type.OBJECT || elementType.sort == Type.ARRAY) {
//                            iv.invokestatic("java/util/Arrays",
//                                    "toString",
//                                    "([Ljava/lang/Object;)Ljava/lang/String;",
//                                    false)
//                            asmType = AsmTypes.JAVA_STRING_TYPE
//                            kotlinType = function.builtIns
//                                    .stringType
//                        } else if (elementType.sort != Type.CHAR) {
//                            iv.invokestatic("java/util/Arrays",
//                                    "toString",
//                                    "(" + asmType.descriptor + ")Ljava/lang/String;",
//                                    false)
//                            asmType = AsmTypes.JAVA_STRING_TYPE
//                            kotlinType = function.builtIns
//                                    .stringType
//                        }
//                    }
//                    AsmUtil.genInvokeAppendMethod(iv, asmType, kotlinType, typeMapper)
//                }
//            }
//        }
//
//        iv.aconst(")")
//        AsmUtil.genInvokeAppendMethod(iv, AsmTypes.JAVA_STRING_TYPE, null)
//
//        iv.invokevirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
//        iv.areturn(AsmTypes.JAVA_STRING_TYPE)
//
//        FunctionCodegen.endVisit(mv, copyMethodName, declaration)
    }

    private fun mapFunctionName(functionDescriptor: FunctionDescriptor): String {
        return typeMapper.mapFunctionName(functionDescriptor, fieldOwnerContext.contextKind)
    }

    private fun visitEndForAnnotationVisitor(annotation: AnnotationVisitor?) {
        annotation?.visitEnd()
    }

    @Suppress("SameParameterValue")
    private fun genOrLoadOnStack(iv: InstructionAdapter,
                                 context: MethodContext,
                                 propertyDescriptor: PropertyDescriptor,
                                 index: Int): JvmKotlinType {
        return if (fieldOwnerContext.contextKind == ERASED_INLINE_CLASS) {
            iv.load(index, underlyingType.type)
            underlyingType
        } else {
            ImplementationBodyCodegen.genPropertyOnStack(iv,
                    context,
                    propertyDescriptor,
                    classAsmType,
                    index,
                    generationState)
        }
    }
}

private fun ClassDescriptor.findCopyFunction(): SimpleFunctionDescriptor? {
    return unsubstitutedMemberScope
            .getContributedFunctions(Name.identifier("copy"), WHEN_GET_ALL_DESCRIPTORS)
            .first()
}
