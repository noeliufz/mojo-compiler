	.data
	.balign 8
FieldAccess.r:
	.zero 24
	.text
FieldAccess:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.0:
	leaq FieldAccess.r(%rip),%rbx
	movq $1,%rax
	movq %rax,0(%rbx)
	leaq FieldAccess.r(%rip),%rbx
	movq $2,%rax
	movq %rax,8(%rbx)
	leaq FieldAccess.r(%rip),%rbx
	movq $3,%rax
	movq %rax,16(%rbx)
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
FieldAccess.badSub:
	call badSub
