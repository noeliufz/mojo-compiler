	.data
	.balign 8
FieldAccess.r:
	.zero 24
	.text
FieldAccess:
	pushq %rbp
	movq %rsp,%rbp
L.0:
	leaq FieldAccess.r(%rip),%rax
	movq $1,0(%rax)
	leaq FieldAccess.r(%rip),%rax
	movq $2,8(%rax)
	leaq FieldAccess.r(%rip),%rax
	movq $3,16(%rax)
#	returnSink
	popq %rbp
	ret
FieldAccess.badSub:
	call badSub
